package ContractGuard.ContractGuard.configs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookConfigRepository extends JpaRepository<WebhookConfig, UUID> {
    List<WebhookConfig> findByOrganizationId(UUID organizationId);

    List<WebhookConfig> findByOrganizationIdAndIsActive(UUID organizationId, Boolean isActive);

    List<WebhookConfig> findByConsumerId(UUID consumerId);

    List<WebhookConfig> findByContractId(UUID contractId);

    List<WebhookConfig> findByOrganizationIdAndEventType(UUID organizationId, String eventType);
}

