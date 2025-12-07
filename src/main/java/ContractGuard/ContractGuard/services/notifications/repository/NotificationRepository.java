package ContractGuard.ContractGuard.services.notifications.repository;

import ContractGuard.ContractGuard.services.notifications.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByOrganizationId(UUID organizationId);

    List<Notification> findByOrganizationIdAndStatus(UUID organizationId, String status);

    List<Notification> findByConsumerId(UUID consumerId);

    @Query("SELECT n FROM Notification n WHERE n.organization.id = :orgId AND n.notificationType = :type ORDER BY n.createdAt DESC")
    List<Notification> findByOrganizationIdAndType(@Param("orgId") UUID organizationId, @Param("type") String notificationType);
}

