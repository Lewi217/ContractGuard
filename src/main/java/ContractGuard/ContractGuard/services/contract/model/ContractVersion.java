package ContractGuard.ContractGuard.services.contract.model;

import ContractGuard.ContractGuard.services.auth.model.User;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contract_versions", indexes = {
    @Index(name = "idx_version_contract", columnList = "contract_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, foreignKey = @ForeignKey(name = "fk_contract_version_contract"))
    private Contract contract;

    @Column(nullable = false, length = 50)
    private String versionNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode openapiSpec;

    @Column(columnDefinition = "TEXT")
    private String changeSummary;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_contract_version_created_by"))
    private User createdBy;
}

