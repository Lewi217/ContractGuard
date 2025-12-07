package ContractGuard.ContractGuard.configs;

import ContractGuard.ContractGuard.services.consumer.model.Consumer;
import ContractGuard.ContractGuard.services.contract.model.Contract;
import ContractGuard.ContractGuard.services.contract.model.Organization;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "webhook_configs", indexes = {
    @Index(name = "idx_webhook_org", columnList = "organization_id"),
    @Index(name = "idx_webhook_consumer", columnList = "consumer_id"),
    @Index(name = "idx_webhook_contract", columnList = "contract_id"),
    @Index(name = "idx_webhook_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_webhook_org"))
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", foreignKey = @ForeignKey(name = "fk_webhook_consumer"))
    private Consumer consumer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", foreignKey = @ForeignKey(name = "fk_webhook_contract"))
    private Contract contract;

    @Column(nullable = false, length = 500)
    private String webhookUrl;

    @Column(nullable = false, length = 50)
    private String eventType;
    // BREAKING_CHANGE_DETECTED, VALIDATION_FAILED, CONTRACT_PUBLISHED, CONTRACT_DEPRECATED

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(length = 500)
    private String secretKey;

    @Column
    @Builder.Default
    private Integer retryCount = 3;

    @Column
    @Builder.Default
    private Integer retryDelaySeconds = 300;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

