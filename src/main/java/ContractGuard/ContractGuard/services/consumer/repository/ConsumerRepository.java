package ContractGuard.ContractGuard.services.consumer.repository;

import ContractGuard.ContractGuard.services.consumer.model.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsumerRepository extends JpaRepository<Consumer, UUID> {
    List<Consumer> findByOrganizationId(UUID organizationId);

    Optional<Consumer> findByApiKey(String apiKey);

    List<Consumer> findByOrganizationIdAndIsActive(UUID organizationId, Boolean isActive);

    Optional<Consumer> findByIdAndOrganizationId(UUID consumerId, UUID organizationId);

    @Query("SELECT c FROM Consumer c WHERE c.organization.id = :orgId AND c.name LIKE %:searchTerm%")
    List<Consumer> searchByName(@Param("orgId") UUID organizationId, @Param("searchTerm") String searchTerm);
}

