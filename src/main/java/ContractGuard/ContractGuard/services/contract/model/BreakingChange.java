package ContractGuard.ContractGuard.services.contract.model;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "breaking_changes", indexes = {
    @Index(name = "idx_breaking_contract", columnList = "contract_id"),
    @Index(name = "idx_breaking_severity", columnList = "severity"),
    @Index(name = "idx_breaking_detected", columnList = "detected_at"),
    @Index(name = "idx_breaking_changes_contract_version", columnList = "contract_id,new_version")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakingChange {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, foreignKey = @ForeignKey(name = "fk_breaking_change_contract"))
    private Contract contract;

    @Column(nullable = false, length = 50)
    private String oldVersion;

    @Column(nullable = false, length = 50)
    private String newVersion;

    @Column(nullable = false, length = 50)
    private String changeType;
    // ENDPOINT_REMOVED, METHOD_CHANGED, FIELD_REMOVED, TYPE_CHANGED, FIELD_REQUIRED, FIELD_OPTIONAL_TO_REQUIRED, etc.

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String severity = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String affectedEndpoint;

    @Column(length = 255)
    private String affectedField;

    @Column(columnDefinition = "TEXT")
    private String migrationGuide;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime detectedAt;
}

