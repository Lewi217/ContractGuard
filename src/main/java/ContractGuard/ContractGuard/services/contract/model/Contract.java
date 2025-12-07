package ContractGuard.ContractGuard.services.contract.model;

import ContractGuard.ContractGuard.services.auth.model.User;
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
@Table(name = "contracts", indexes = {
    @Index(name = "idx_contract_org", columnList = "organization_id"),
    @Index(name = "idx_contract_status", columnList = "status"),
    @Index(name = "idx_contract_name", columnList = "name"),
    @Index(name = "idx_contract_org_status", columnList = "organization_id,status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_contract_org"))
    private Organization organization;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String version;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, ACTIVE, DEPRECATED, RETIRED

    @Column(nullable = false, length = 255)
    private String basePath;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode openapiSpec;

    @Column(length = 500)
    private String blobStorageUrl;

    @Column(columnDefinition = "TEXT[]")
    private String[] tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_contract_created_by"))
    private User createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deprecatedAt;

    @Column
    private LocalDateTime retiredAt;
}

