package ContractGuard.ContractGuard.services.contract.service.impl;

import ContractGuard.ContractGuard.services.contract.dto.ContractResponse;
import ContractGuard.ContractGuard.services.contract.dto.CreateContractRequest;
import ContractGuard.ContractGuard.services.contract.model.Contract;
import ContractGuard.ContractGuard.services.contract.model.Organization;
import ContractGuard.ContractGuard.services.contract.repository.ContractRepository;
import ContractGuard.ContractGuard.services.contract.repository.OrganizationRepository;
import ContractGuard.ContractGuard.shared.enums.ContractStatus;
import ContractGuard.ContractGuard.shared.exception.BadRequestException;
import ContractGuard.ContractGuard.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContractServiceImpl {

    private final ContractRepository contractRepository;
    private final OrganizationRepository organizationRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ContractResponse createContract(CreateContractRequest request) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        contractRepository.findByNameAndVersionAndOrganizationId(
                request.getName(),
                request.getVersion(),
                request.getOrganizationId()
        ).ifPresent(c -> {
            throw new BadRequestException("Contract with this name and version already exists");
        });

        JsonNode openapiSpec = fetchOpenApiSpecStrict(request.getOpenapiUrl());
        ArrayNode tagsNode = buildTagsStrict(request.getTags());

        Contract contract = Contract.builder()
                .organization(org)
                .name(request.getName())
                .version(request.getVersion())
                .basePath(request.getBasePath())
                .description(request.getDescription())
                .openapiSpec(openapiSpec)
                .tags(tagsNode)
                .status(ContractStatus.DRAFT)
                .build();

        return mapToResponse(contractRepository.save(contract));
    }

    @Cacheable(value = "contracts", key = "#contractId")
    public ContractResponse getContract(UUID contractId) {
        return contractRepository.findById(contractId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByOrganization(UUID organizationId) {
        return contractRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByOrganizationPaginated(UUID organizationId, Pageable pageable) {
        return contractRepository.findByOrganizationId(organizationId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByStatus(UUID organizationId, String status) {
        return contractRepository.findByOrganizationIdAndStatus(
                        organizationId,
                        ContractStatus.valueOf(status.toUpperCase())
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> searchContractsByName(UUID organizationId, String searchTerm) {
        return contractRepository.searchByName(organizationId, searchTerm)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteContract(UUID contractId) {
        Contract contract = getContractEntity(contractId);
        contractRepository.delete(contract);
    }

    @CacheEvict(value = "contracts", key = "#contractId")
    public ContractResponse updateContract(UUID contractId, CreateContractRequest request) {
        Contract contract = getContractEntity(contractId);
        JsonNode openapiSpec = fetchOpenApiSpecStrict(request.getOpenapiUrl());
        ArrayNode tagsNode = buildTagsStrict(request.getTags());
        contract.setName(request.getName());
        contract.setVersion(request.getVersion());
        contract.setBasePath(request.getBasePath());
        contract.setDescription(request.getDescription());
        contract.setOpenapiSpec(openapiSpec);
        contract.setTags(tagsNode);

        return mapToResponse(contractRepository.save(contract));
    }

    @CacheEvict(value = "contracts", key = "#contractId")
    public ContractResponse publishContract(UUID contractId) {
        Contract contract = getContractEntity(contractId);
        contract.setStatus(ContractStatus.ACTIVE);
        return mapToResponse(contractRepository.save(contract));
    }

    @CacheEvict(value = "contracts", key = "#contractId")
    public ContractResponse deprecateContract(UUID contractId) {
        Contract contract = getContractEntity(contractId);
        contract.setStatus(ContractStatus.DEPRECATED);
        contract.setDeprecatedAt(LocalDateTime.now());
        return mapToResponse(contractRepository.save(contract));
    }

    @CacheEvict(value = "contracts", key = "#contractId")
    public ContractResponse retireContract(UUID contractId) {
        Contract contract = getContractEntity(contractId);
        contract.setStatus(ContractStatus.RETIRED);
        contract.setRetiredAt(LocalDateTime.now());
        return mapToResponse(contractRepository.save(contract));
    }


    private JsonNode fetchOpenApiSpecStrict(String url) {
        if (url == null || url.isBlank()) {
            throw new BadRequestException("OpenAPI URL must be provided");
        }
        try {
            // Fetch as String first, then parse with ObjectMapper
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getBody() == null || response.getBody().isBlank()) {
                throw new BadRequestException("OpenAPI spec could not be fetched from URL: " + url);
            }
            JsonNode spec = objectMapper.readTree(response.getBody());
            if (spec == null) {
                throw new BadRequestException("OpenAPI spec could not be parsed from URL: " + url);
            }
            if (!spec.has("openapi") && !spec.has("swagger")) {
                throw new BadRequestException("OpenAPI spec missing 'openapi' or 'swagger' version field");
            }
            if (!spec.has("paths") || spec.get("paths").isEmpty()) {
                throw new BadRequestException("OpenAPI spec contains no paths");
            }
            if (!spec.has("components") || !spec.get("components").has("schemas")) {
                throw new BadRequestException("OpenAPI spec missing components/schemas");
            }

            log.info("Successfully fetched and parsed OpenAPI spec from: {}", url);
            return spec;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to fetch OpenAPI spec from {}: {}", url, ex.getMessage(), ex);
            throw new BadRequestException("Failed to fetch OpenAPI spec from " + url + ": " + ex.getMessage());
        }
    }

    private ArrayNode buildTagsStrict(String[] tags) {
        ArrayNode node = objectMapper.createArrayNode();
        if (tags == null || tags.length == 0) {
            return node;
        }
        for (String tag : tags) {
            if (tag == null || tag.isBlank()) {
                throw new BadRequestException("Tags cannot be blank");
            }
            node.add(tag);
        }
        return node;
    }

    private Contract getContractEntity(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
    }

    private ContractResponse mapToResponse(Contract contract) {
        String[] tagsArray = contract.getTags() != null
                ? objectMapper.convertValue(contract.getTags(), String[].class)
                : new String[]{};

        return ContractResponse.builder()
                .id(contract.getId())
                .name(contract.getName())
                .version(contract.getVersion())
                .status(contract.getStatus().name())
                .organizationId(contract.getOrganization().getId())
                .basePath(contract.getBasePath())
                .openapiSpec(contract.getOpenapiSpec())
                .blobStorageUrl(contract.getBlobStorageUrl())
                .tags(tagsArray)
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .deprecatedAt(contract.getDeprecatedAt())
                .retiredAt(contract.getRetiredAt())
                .build();
    }
}