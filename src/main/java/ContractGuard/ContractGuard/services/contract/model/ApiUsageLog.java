package ContractGuard.ContractGuard.services.contract.model;

import ContractGuard.ContractGuard.services.consumer.model.Consumer;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_usage_logs", indexes = {
    @Index(name = "idx_usage_contract", columnList = "contract_id"),
    @Index(name = "idx_usage_consumer", columnList = "consumer_id"),
    @Index(name = "idx_usage_created", columnList = "created_at"),
    @Index(name = "idx_usage_endpoint", columnList = "endpoint_path")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, foreignKey = @ForeignKey(name = "fk_usage_contract"))
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", foreignKey = @ForeignKey(name = "fk_usage_consumer"))
    private Consumer consumer;

    @Column(nullable = false, length = 500)
    private String endpointPath;

    @Column(nullable = false, length = 10)
    private String httpMethod;

    @Column
    private Integer statusCode;

    @Column
    private Integer responseTimeMs;

    @Column
    private Integer requestSizeBytes;

    @Column
    private Integer responseSizeBytes;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 50)
    private String ipAddress;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

