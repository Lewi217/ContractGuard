package ContractGuard.ContractGuard.services.changeDetection.model;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "change_details", indexes = {
    @Index(name = "idx_change_detail_breaking_change", columnList = "breaking_change_id"),
    @Index(name = "idx_change_detail_type", columnList = "change_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breaking_change_id", nullable = false, foreignKey = @ForeignKey(name = "fk_change_detail_breaking_change"))
    private BreakingChangeDetailed breakingChange;

    @Column(nullable = false, length = 100)
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(nullable = false, length = 50)
    private String changeType;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String severity = "MEDIUM";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode metadata;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

