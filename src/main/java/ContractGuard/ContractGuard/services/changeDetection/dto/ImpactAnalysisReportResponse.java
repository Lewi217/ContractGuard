package ContractGuard.ContractGuard.services.changeDetection.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactAnalysisReportResponse {
    private UUID contractId;
    private String contractName;
    private String oldVersion;
    private String newVersion;
    private Integer totalImpactedConsumers;
    private Integer criticalImpactCount;
    private Integer highImpactCount;
    private Integer mediumImpactCount;
    private Integer lowImpactCount;
    private Integer estimatedTotalMigrationEffort;
    private List<ImpactAnalysisResponse> impactAnalyses;
    private String recommendedDeploymentApproach;
}

