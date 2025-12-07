package ContractGuard.ContractGuard.services.contract.service.impl;

import ContractGuard.ContractGuard.services.contract.dto.CreateContractRequest;
import ContractGuard.ContractGuard.services.contract.dto.ContractResponse;
import ContractGuard.ContractGuard.services.contract.model.Contract;
import ContractGuard.ContractGuard.services.contract.model.Organization;
import ContractGuard.ContractGuard.shared.exception.BadRequestException;
import ContractGuard.ContractGuard.shared.exception.ResourceNotFoundException;
import ContractGuard.ContractGuard.services.contract.repository.ContractRepository;
import ContractGuard.ContractGuard.services.contract.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Create a new contract
     */
    @CacheEvict(value = "contracts", allEntries = true)
    public ContractResponse createContract(CreateContractRequest request) {
        log.info("Creating contract: {} version {}", request.getName(), request.getVersion());

        // Validate organization exists
        Organization org = organizationRepository.findById(request.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        // Check if contract with same name and version already exists
        contractRepository.findByNameAndVersionAndOrganizationId(
            request.getName(),
            request.getVersion(),
            request.getOrganizationId()
        ).ifPresent(contract -> {
            throw new BadRequestException("Contract with name and version already exists");
        });

        Contract contract = Contract.builder()
            .organization(org)
            .name(request.getName())
            .version(request.getVersion())
            .basePath(request.getBasePath())
            .openapiSpec(request.getOpenapiSpec())
            .description(request.getDescription())
            .tags(request.getTags())
            .status("DRAFT")
            .build();

        Contract savedContract = contractRepository.save(contract);
        log.info("Contract created successfully: {}", savedContract.getId());

        return mapToResponse(savedContract);
    }

    /**
     * Get contract by ID
     */
    @Cacheable(value = "contracts", key = "#contractId")
    public ContractResponse getContract(UUID contractId) {
        log.info("Fetching contract: {}", contractId);
        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        return mapToResponse(contract);
    }

    /**
     * Get all contracts for organization
     */
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByOrganization(UUID organizationId) {
        log.info("Fetching contracts for organization: {}", organizationId);
        return contractRepository.findByOrganizationId(organizationId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get paginated contracts for organization
     */
    @Transactional(readOnly = true)
    public Page<ContractResponse> getContractsByOrganizationPaginated(UUID organizationId, Pageable pageable) {
        log.info("Fetching paginated contracts for organization: {}", organizationId);
        return contractRepository.findByOrganizationId(organizationId, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Get contracts by status
     */
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByStatus(UUID organizationId, String status) {
        log.info("Fetching contracts with status {} for organization: {}", status, organizationId);
        return contractRepository.findByOrganizationIdAndStatus(organizationId, status)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Search contracts by name
     */
    @Transactional(readOnly = true)
    public List<ContractResponse> searchContractsByName(UUID organizationId, String searchTerm) {
        log.info("Searching contracts with name: {}", searchTerm);
        return contractRepository.searchByName(organizationId, searchTerm)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Update contract
     */
    @CacheEvict(value = "contracts", key = "#contractId")
    public ContractResponse updateContract(UUID contractId, CreateContractRequest request) {
        log.info("Updating contract: {}", contractId);

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        contract.setName(request.getName());
        contract.setVersion(request.getVersion());
        contract.setBasePath(request.getBasePath());
        contract.setOpenapiSpec(request.getOpenapiSpec());
        contract.setDescription(request.getDescription());
        contract.setTags(request.getTags());

        Contract updatedContract = contractRepository.save(contract);
        log.info("Contract updated successfully: {}", contractId);

        return mapToResponse(updatedContract);
    }

    /**
     * Delete contract
     */
    @CacheEvict(value = "contracts", allEntries = true)
    public void deleteContract(UUID contractId) {
        log.info("Deleting contract: {}", contractId);

        if (!contractRepository.existsById(contractId)) {
            throw new ResourceNotFoundException("Contract not found");
        }

        contractRepository.deleteById(contractId);
        log.info("Contract deleted successfully: {}", contractId);
    }

    /**
     * Publish contract (change status to ACTIVE)
     */
    @CacheEvict(value = "contracts", key = "#contractId")
    public ContractResponse publishContract(UUID contractId) {
        log.info("Publishing contract: {}", contractId);

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        contract.setStatus("ACTIVE");
        Contract updatedContract = contractRepository.save(contract);

        log.info("Contract published successfully: {}", contractId);
        return mapToResponse(updatedContract);
    }

    /**
     * Deprecate contract
     */
    @CacheEvict(value = "contracts", key = "#contractId")
    public ContractResponse deprecateContract(UUID contractId) {
        log.info("Deprecating contract: {}", contractId);

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        contract.setStatus("DEPRECATED");
        contract.setDeprecatedAt(java.time.LocalDateTime.now());
        Contract updatedContract = contractRepository.save(contract);

        log.info("Contract deprecated successfully: {}", contractId);
        return mapToResponse(updatedContract);
    }

    /**
     * Retire contract
     */
    @CacheEvict(value = "contracts", key = "#contractId")
    public ContractResponse retireContract(UUID contractId) {
        log.info("Retiring contract: {}", contractId);

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        contract.setStatus("RETIRED");
        contract.setRetiredAt(java.time.LocalDateTime.now());
        Contract updatedContract = contractRepository.save(contract);

        log.info("Contract retired successfully: {}", contractId);
        return mapToResponse(updatedContract);
    }

    private ContractResponse mapToResponse(Contract contract) {
        return ContractResponse.builder()
            .id(contract.getId())
            .name(contract.getName())
            .version(contract.getVersion())
            .status(contract.getStatus())
            .organizationId(contract.getOrganization().getId())
            .basePath(contract.getBasePath())
            .openapiSpec(contract.getOpenapiSpec())
            .blobStorageUrl(contract.getBlobStorageUrl())
            .tags(contract.getTags())
            .createdAt(contract.getCreatedAt())
            .updatedAt(contract.getUpdatedAt())
            .deprecatedAt(contract.getDeprecatedAt())
            .retiredAt(contract.getRetiredAt())
            .build();
    }
}

