package ContractGuard.ContractGuard.services.consumer.repository;

import ContractGuard.ContractGuard.services.consumer.model.ConsumerRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsumerRegistrationRepository extends JpaRepository<ConsumerRegistration, UUID> {
    List<ConsumerRegistration> findByContractId(UUID contractId);

    List<ConsumerRegistration> findByConsumerId(UUID consumerId);

    List<ConsumerRegistration> findByContractIdAndStatus(UUID contractId, String status);

    Optional<ConsumerRegistration> findByConsumerIdAndContractId(UUID consumerId, UUID contractId);

    @Query("SELECT cr FROM ConsumerRegistration cr WHERE cr.contract.id = :contractId AND cr.status = 'ACTIVE'")
    List<ConsumerRegistration> findActiveConsumersForContract(@Param("contractId") UUID contractId);
}

