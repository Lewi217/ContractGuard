package ContractGuard.ContractGuard.services.changeDetection.model;

import ContractGuard.ContractGuard.services.contract.model.Contract;
import ContractGuard.ContractGuard.services.consumer.model.Consumer;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "impact_analyses", indexes = {
    @Index(name = "idx_ia_contract", columnList = "contract_id"),
    @Index(name = "idx_ia_breaking_change", columnList = "breaking_change_id"),
    @Index(name = "idx_ia_consumer", columnList = "consumer_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ia_contract"))
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breaking_change_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ia_breaking_change"))
    private BreakingChangeDetailed breakingChange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ia_consumer"))
    private Consumer consumer;

    @Column(nullable = false)
    private Integer impactScore;

    @Column(length = 20)
    @Builder.Default
    private String impactLevel = "MEDIUM";

    @Column(length = 50)
    @Builder.Default
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String affectedEndpoints;

    @Column
    private Integer estimatedMigrationEffort;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

