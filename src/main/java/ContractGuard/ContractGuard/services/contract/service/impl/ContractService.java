package ContractGuard.ContractGuard.services.contract.service.impl;

import ContractGuard.ContractGuard.services.contract.dto.CreateContractRequest;
import ContractGuard.ContractGuard.services.contract.dto.ContractResponse;
import ContractGuard.ContractGuard.services.contract.model.Contract;
import ContractGuard.ContractGuard.services.contract.model.Organization;
import ContractGuard.ContractGuard.services.contract.repository.ContractRepository;
import ContractGuard.ContractGuard.services.contract.repository.OrganizationRepository;
import ContractGuard.ContractGuard.shared.enums.ContractStatus;
import ContractGuard.ContractGuard.shared.exception.BadRequestException;
import ContractGuard.ContractGuard.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;
    private final OrganizationRepository organizationRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContractResponse createContract(CreateContractRequest request) {

        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        contractRepository.findByNameAndVersionAndOrganizationId(
                request.getName(),
                request.getVersion(),
                request.getOrganizationId()
        ).ifPresent(c -> {
            throw new BadRequestException("Contract with name and version already exists");
        });

        JsonNode openapiSpec = fetchOpenApiSpec(request.getOpenapiUrl());
        ArrayNode tagsNode = objectMapper.createArrayNode();
        if (request.getTags() != null) {
            for (String tag : request.getTags()) {
                tagsNode.add(tag);
            }
        }

        Contract contract = Contract.builder()
                .organization(org)
                .name(request.getName())
                .version(request.getVersion())
                .basePath(request.getBasePath())
                .openapiSpec(openapiSpec)
                .description(request.getDescription())
                .tags(tagsNode)
                .status(ContractStatus.DRAFT)
                .build();

        return mapToResponse(contractRepository.save(contract));
    }

    @Cacheable(value = "contracts", key = "#contractId")
    public ContractResponse getContract(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        return mapToResponse(contract);
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
        return contractRepository.findByOrganizationIdAndStatus(organizationId, status)
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
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        contractRepository.delete(contract);
    }

    @CacheEvict(value = "contracts", key = "#contractId")
    public ContractResponse updateContract(UUID contractId, CreateContractRequest request) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        JsonNode openapiSpec = fetchOpenApiSpec(request.getOpenapiUrl());
        ArrayNode tagsNode = objectMapper.createArrayNode();
        if (request.getTags() != null) {
            for (String tag : request.getTags()) {
                tagsNode.add(tag);
            }
        }

        contract.setName(request.getName());
        contract.setVersion(request.getVersion());
        contract.setBasePath(request.getBasePath());
        contract.setOpenapiSpec(openapiSpec);
        contract.setDescription(request.getDescription());
        contract.setTags(tagsNode);

        return mapToResponse(contractRepository.save(contract));
    }

    @CacheEvict(value = "contracts", key = "#contractId")
    public ContractResponse publishContract(UUID contractId) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        contract.setStatus(ContractStatus.ACTIVE);
        return mapToResponse(contractRepository.save(contract));
    }

    @CacheEvict(value = "contracts", key = "#contractId")
    public ContractResponse deprecateContract(UUID contractId) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        contract.setStatus(ContractStatus.DEPRECATED);
        contract.setDeprecatedAt(LocalDateTime.now());

        return mapToResponse(contractRepository.save(contract));
    }

    @CacheEvict(value = "contracts", key = "#contractId")
    public ContractResponse retireContract(UUID contractId) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        contract.setStatus(ContractStatus.RETIRED);
        contract.setRetiredAt(LocalDateTime.now());

        return mapToResponse(contractRepository.save(contract));
    }

    private JsonNode fetchOpenApiSpec(String url) {
        try {
            return restTemplate.getForObject(url, JsonNode.class);
        } catch (Exception e) {
            throw new BadRequestException("Failed to fetch OpenAPI spec from: " + url);
        }
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
