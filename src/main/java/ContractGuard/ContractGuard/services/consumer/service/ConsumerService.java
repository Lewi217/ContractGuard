package ContractGuard.ContractGuard.services.consumer.service;

import ContractGuard.ContractGuard.services.consumer.dto.ConsumerResponse;
import ContractGuard.ContractGuard.services.consumer.dto.CreateConsumerRequest;
import ContractGuard.ContractGuard.services.consumer.dto.UpdateConsumerRequest;
import ContractGuard.ContractGuard.services.consumer.model.Consumer;

import java.util.List;
import java.util.UUID;

public interface ConsumerService {
    ConsumerResponse registerConsumer(CreateConsumerRequest request);
    ConsumerResponse getConsumer(UUID consumerId);
    List<ConsumerResponse> getConsumersByOrganization(UUID organizationId);
    List<ConsumerResponse> getActiveConsumers(UUID organizationId);
    List<ConsumerResponse> searchConsumersByName(UUID organizationId, String searchTerm);
    Consumer getConsumerByApiKey(String apiKey);
    ConsumerResponse updateConsumer(UUID consumerId, UpdateConsumerRequest request);
    ConsumerResponse deactivateConsumer(UUID consumerId);
    void deleteConsumer(UUID consumerId);
}
