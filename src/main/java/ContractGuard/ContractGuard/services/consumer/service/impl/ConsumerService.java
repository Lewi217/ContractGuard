package ContractGuard.ContractGuard.services.consumer.service.impl;

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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConsumerService {

    private final ConsumerRepository consumerRepository;
    private final OrganizationRepository organizationRepository;

    /**
     * Register a new consumer
     */
    @CacheEvict(value = "consumers", allEntries = true)
    public Consumer registerConsumer(String name, UUID organizationId, String contactEmail) {
        log.info("Registering consumer: {} for organization: {}", name, organizationId);

        Organization org = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Consumer consumer = Consumer.builder()
            .organization(org)
            .name(name)
            .contactEmail(contactEmail)
            .consumerType("SERVICE")
            .isActive(true)
            .build();

        Consumer savedConsumer = consumerRepository.save(consumer);
        log.info("Consumer registered successfully: {}", savedConsumer.getId());

        return savedConsumer;
    }

    /**
     * Get consumer by ID
     */
    @Cacheable(value = "consumers", key = "#consumerId")
    public Consumer getConsumer(UUID consumerId) {
        log.info("Fetching consumer: {}", consumerId);
        return consumerRepository.findById(consumerId)
            .orElseThrow(() -> new ResourceNotFoundException("Consumer not found"));
    }

    /**
     * Get consumers for organization
     */
    @Transactional(readOnly = true)
    public List<Consumer> getConsumersByOrganization(UUID organizationId) {
        log.info("Fetching consumers for organization: {}", organizationId);
        return consumerRepository.findByOrganizationId(organizationId);
    }

    /**
     * Get active consumers for organization
     */
    @Transactional(readOnly = true)
    public List<Consumer> getActiveConsumers(UUID organizationId) {
        log.info("Fetching active consumers for organization: {}", organizationId);
        return consumerRepository.findByOrganizationIdAndIsActive(organizationId, true);
    }

    /**
     * Search consumers by name
     */
    @Transactional(readOnly = true)
    public List<Consumer> searchConsumersByName(UUID organizationId, String searchTerm) {
        log.info("Searching consumers with name: {}", searchTerm);
        return consumerRepository.searchByName(organizationId, searchTerm);
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
    public Consumer updateConsumer(UUID consumerId, String name, String contactEmail, String contactName) {
        log.info("Updating consumer: {}", consumerId);

        Consumer consumer = consumerRepository.findById(consumerId)
            .orElseThrow(() -> new ResourceNotFoundException("Consumer not found"));

        consumer.setName(name);
        consumer.setContactEmail(contactEmail);
        consumer.setContactName(contactName);

        Consumer updatedConsumer = consumerRepository.save(consumer);
        log.info("Consumer updated successfully: {}", consumerId);

        return updatedConsumer;
    }

    /**
     * Deactivate consumer
     */
    @CacheEvict(value = "consumers", key = "#consumerId")
    public Consumer deactivateConsumer(UUID consumerId) {
        log.info("Deactivating consumer: {}", consumerId);

        Consumer consumer = consumerRepository.findById(consumerId)
            .orElseThrow(() -> new ResourceNotFoundException("Consumer not found"));

        consumer.setIsActive(false);
        Consumer updatedConsumer = consumerRepository.save(consumer);

        log.info("Consumer deactivated successfully: {}", consumerId);
        return updatedConsumer;
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
}

