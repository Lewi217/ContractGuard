package ContractGuard.ContractGuard.services.contract.model;

import ContractGuard.ContractGuard.services.auth.model.User;
import ContractGuard.ContractGuard.shared.enums.ContractStatus;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "contracts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_contract_name_version_org",
                        columnNames = {"organization_id", "name", "version"}
                )
        },
        indexes = {
                @Index(name = "idx_contract_org", columnList = "organization_id"),
                @Index(name = "idx_contract_status", columnList = "status"),
                @Index(name = "idx_contract_name", columnList = "name"),
                @Index(name = "idx_contract_org_status", columnList = "organization_id,status")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContractStatus status = ContractStatus.DRAFT;

    @Column(nullable = false)
    private String basePath;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode openapiSpec;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode tags; // Updated to JSON instead of @ElementCollection

    @Column(length = 500)
    private String blobStorageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deprecatedAt;
    private LocalDateTime retiredAt;
}
