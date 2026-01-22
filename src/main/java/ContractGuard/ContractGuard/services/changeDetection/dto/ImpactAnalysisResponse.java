package ContractGuard.ContractGuard.services.changeDetection.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactAnalysisResponse {
    private UUID id;
    private UUID contractId;
    private UUID breakingChangeId;
    private UUID consumerId;
    private String consumerName;
    private Integer impactScore;
    private String impactLevel;
    private String status;
    private String affectedEndpoints;
    private Integer estimatedMigrationEffort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

