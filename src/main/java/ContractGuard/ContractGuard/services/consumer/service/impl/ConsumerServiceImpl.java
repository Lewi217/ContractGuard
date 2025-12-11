package ContractGuard.ContractGuard.services.consumer.service.impl;

import ContractGuard.ContractGuard.services.consumer.dto.CreateConsumerRequest;
import ContractGuard.ContractGuard.services.consumer.dto.UpdateConsumerRequest;
import ContractGuard.ContractGuard.services.consumer.dto.ConsumerResponse;
import ContractGuard.ContractGuard.services.consumer.model.Consumer;
import ContractGuard.ContractGuard.services.contract.model.Organization;
import ContractGuard.ContractGuard.shared.exception.ResourceNotFoundException;
import ContractGuard.ContractGuard.services.consumer.repository.ConsumerRepository;
import ContractGuard.ContractGuard.services.contract.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConsumerServiceImpl {

    private final ConsumerRepository consumerRepository;
    private final OrganizationRepository organizationRepository;

    /**
     * Register a new consumer
     */
    @CacheEvict(value = "consumers", allEntries = true)
    public ConsumerResponse registerConsumer(CreateConsumerRequest request) {
        log.info("Registering consumer: {} for organization: {}", request.getName(), request.getOrganizationId());

        Organization org = organizationRepository.findById(request.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Consumer consumer = Consumer.builder()
            .organization(org)
            .name(request.getName())
            .description(request.getDescription())
            .contactEmail(request.getContactEmail())
            .contactName(request.getContactName())
            .consumerType(request.getConsumerType() != null ? request.getConsumerType() : "SERVICE")
            .isActive(true)
            .build();

        Consumer savedConsumer = consumerRepository.save(consumer);
        log.info("Consumer registered successfully: {}", savedConsumer.getId());

        return mapToResponse(savedConsumer);
    }

    /**
     * Get consumer by ID
     */
    @Cacheable(value = "consumers", key = "#consumerId")
    public ConsumerResponse getConsumer(UUID consumerId) {
        log.info("Fetching consumer: {}", consumerId);
        Consumer consumer = consumerRepository.findById(consumerId)
            .orElseThrow(() -> new ResourceNotFoundException("Consumer not found"));
        return mapToResponse(consumer);
    }

    /**
     * Get consumers for organization
     */
    @Transactional(readOnly = true)
    public List<ConsumerResponse> getConsumersByOrganization(UUID organizationId) {
        log.info("Fetching consumers for organization: {}", organizationId);
        return consumerRepository.findByOrganizationId(organizationId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get active consumers for organization
     */
    @Transactional(readOnly = true)
    public List<ConsumerResponse> getActiveConsumers(UUID organizationId) {
        log.info("Fetching active consumers for organization: {}", organizationId);
        return consumerRepository.findByOrganizationIdAndIsActive(organizationId, true)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Search consumers by name
     */
    @Transactional(readOnly = true)
    public List<ConsumerResponse> searchConsumersByName(UUID organizationId, String searchTerm) {
        log.info("Searching consumers with name: {}", searchTerm);
        return consumerRepository.searchByName(organizationId, searchTerm)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get consumer by API key
     */
    @Transactional(readOnly = true)
    public Consumer getConsumerByApiKey(String apiKey) {
        log.info("Fetching consumer by API key");
        return consumerRepository.findByApiKey(apiKey)
            .orElseThrow(() -> new ResourceNotFoundException("Consumer not found"));
    }

    /**
     * Update consumer
     */
    @CacheEvict(value = "consumers", key = "#consumerId")
    public ConsumerResponse updateConsumer(UUID consumerId, UpdateConsumerRequest request) {
        log.info("Updating consumer: {}", consumerId);

        Consumer consumer = consumerRepository.findById(consumerId)
            .orElseThrow(() -> new ResourceNotFoundException("Consumer not found"));

        if (request.getName() != null) {
            consumer.setName(request.getName());
        }
        if (request.getDescription() != null) {
            consumer.setDescription(request.getDescription());
        }
        if (request.getContactEmail() != null) {
            consumer.setContactEmail(request.getContactEmail());
        }
        if (request.getContactName() != null) {
            consumer.setContactName(request.getContactName());
        }
        if (request.getConsumerType() != null) {
            consumer.setConsumerType(request.getConsumerType());
        }

        Consumer updatedConsumer = consumerRepository.save(consumer);
        log.info("Consumer updated successfully: {}", consumerId);

        return mapToResponse(updatedConsumer);
    }

    /**
     * Deactivate consumer
     */
    @CacheEvict(value = "consumers", key = "#consumerId")
    public ConsumerResponse deactivateConsumer(UUID consumerId) {
        log.info("Deactivating consumer: {}", consumerId);

        Consumer consumer = consumerRepository.findById(consumerId)
            .orElseThrow(() -> new ResourceNotFoundException("Consumer not found"));

        consumer.setIsActive(false);
        Consumer updatedConsumer = consumerRepository.save(consumer);

        log.info("Consumer deactivated successfully: {}", consumerId);
        return mapToResponse(updatedConsumer);
    }

    /**
     * Delete consumer
     */
    @CacheEvict(value = "consumers", allEntries = true)
    public void deleteConsumer(UUID consumerId) {
        log.info("Deleting consumer: {}", consumerId);

        if (!consumerRepository.existsById(consumerId)) {
            throw new ResourceNotFoundException("Consumer not found");
        }

        consumerRepository.deleteById(consumerId);
        log.info("Consumer deleted successfully: {}", consumerId);
    }

    /**
     * Map Consumer entity to ConsumerResponse DTO
     */
    private ConsumerResponse mapToResponse(Consumer consumer) {
        return ConsumerResponse.builder()
            .id(consumer.getId())
            .organizationId(consumer.getOrganization().getId())
            .name(consumer.getName())
            .description(consumer.getDescription())
            .apiKey(consumer.getApiKey())
            .contactEmail(consumer.getContactEmail())
            .contactName(consumer.getContactName())
            .consumerType(consumer.getConsumerType())
            .isActive(consumer.getIsActive())
            .createdAt(consumer.getCreatedAt())
            .updatedAt(consumer.getUpdatedAt())
            .build();
    }
}

