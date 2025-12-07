package ContractGuard.ContractGuard.services.validation.model;

import ContractGuard.ContractGuard.services.consumer.model.Consumer;
import ContractGuard.ContractGuard.services.contract.model.Contract;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "validation_results", indexes = {
    @Index(name = "idx_validation_contract", columnList = "contract_id"),
    @Index(name = "idx_validation_consumer", columnList = "consumer_id"),
    @Index(name = "idx_validation_status", columnList = "status"),
    @Index(name = "idx_validation_created", columnList = "created_at"),
    @Index(name = "idx_validation_contract_created", columnList = "contract_id,created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, foreignKey = @ForeignKey(name = "fk_validation_contract"))
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", foreignKey = @ForeignKey(name = "fk_validation_consumer"))
    private Consumer consumer;

    @Column(nullable = false, length = 500)
    private String endpointPath;

    @Column(nullable = false, length = 10)
    private String httpMethod;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PASSED, FAILED, ERROR

    @Column
    private Boolean passed;

    @Column
    private Integer expectedStatusCode;

    @Column
    private Integer actualStatusCode;

    @Column
    private Integer responseTimeMs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode violations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode testData;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

