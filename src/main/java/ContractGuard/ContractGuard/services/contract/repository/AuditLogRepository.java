package ContractGuard.ContractGuard.services.contract.repository;

import ContractGuard.ContractGuard.services.contract.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByOrganizationId(UUID organizationId);

    List<AuditLog> findByOrganizationIdAndAction(UUID organizationId, String action);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    @Query("SELECT a FROM AuditLog a WHERE a.organization.id = :orgId AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AuditLog> findByOrganizationIdAndDateRange(@Param("orgId") UUID organizationId, @Param("since") LocalDateTime since);
}

