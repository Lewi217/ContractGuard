package ContractGuard.ContractGuard.services.notifications.model;

import ContractGuard.ContractGuard.services.contract.model.Organization;
import ContractGuard.ContractGuard.services.consumer.model.Consumer;
import ContractGuard.ContractGuard.services.contract.model.Contract;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_org", columnList = "organization_id"),
    @Index(name = "idx_notification_status", columnList = "status"),
    @Index(name = "idx_notification_type", columnList = "notification_type"),
    @Index(name = "idx_notification_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notification_org"))
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", foreignKey = @ForeignKey(name = "fk_notification_consumer"))
    private Consumer consumer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", foreignKey = @ForeignKey(name = "fk_notification_contract"))
    private Contract contract;

    @Column(nullable = false, length = 50)
    private String notificationType;
    // BREAKING_CHANGE_ALERT, VALIDATION_FAILURE, DEPRECATION_WARNING, GENERAL_INFO

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SENT, FAILED, DELIVERED

    @Column(nullable = false, length = 50)
    private String channel; // EMAIL, SLACK, WEBHOOK, IN_APP

    @Column(length = 255)
    private String recipientEmail;

    @Column(length = 255)
    private String recipientSlackId;

    @Column(length = 500)
    private String webhookUrl;

    @Column(length = 500)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode payload;

    @Column
    private LocalDateTime sentAt;

    @Column(length = 50)
    private String deliveryStatus;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

