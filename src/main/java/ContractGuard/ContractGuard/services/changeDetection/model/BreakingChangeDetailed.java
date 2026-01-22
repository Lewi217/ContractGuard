package ContractGuard.ContractGuard.services.changeDetection.model;

import ContractGuard.ContractGuard.services.contract.model.Contract;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "breaking_changes_detailed", indexes = {
    @Index(name = "idx_bcd_contract", columnList = "contract_id"),
    @Index(name = "idx_bcd_severity", columnList = "severity"),
    @Index(name = "idx_bcd_detected_at", columnList = "detected_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakingChangeDetailed {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bcd_contract"))
    private Contract contract;

    @Column(nullable = false, length = 50)
    private String oldVersion;

    @Column(nullable = false, length = 50)
    private String newVersion;

    @Column(nullable = false, length = 100)
    private String changeType;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String severity = "MEDIUM";

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String affectedEndpoint;

    @Column(length = 255)
    private String affectedField;

    @Column(columnDefinition = "TEXT")
    private String migrationGuide;

    @Column(columnDefinition = "TEXT")
    private String codeExample;

    @Column(length = 20)
    @Builder.Default
    private String impactLevel = "MEDIUM";

    @Column(length = 500)
    private String deprecationPath;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode additionalContext;

    @OneToMany(mappedBy = "breakingChange", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChangeDetail> changeDetails;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime detectedAt;
}

