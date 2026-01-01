package ContractGuard.ContractGuard.services.changeDetection.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeDetectionReport {

    private UUID contractId;
    private String contractName;
    private String oldVersion;
    private String newVersion;
    private Integer totalChanges;
    private Integer criticalChanges;
    private Integer highSeverityChanges;
    private Integer mediumSeverityChanges;
    private Integer lowSeverityChanges;
    private List<BreakingChangeResponse> breakingChanges;
    private LocalDateTime detectedAt;
    private String summary;
}

