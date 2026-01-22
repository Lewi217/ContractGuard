package ContractGuard.ContractGuard.services.changeDetection.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakingChangeDetailedResponse {
    private UUID id;
    private UUID contractId;
    private String oldVersion;
    private String newVersion;
    private String changeType;
    private String severity;
    private String description;
    private String affectedEndpoint;
    private String affectedField;
    private String migrationGuide;
    private String codeExample;
    private String impactLevel;
    private String deprecationPath;
    private List<ChangeDetailResponse> changeDetails;
    private LocalDateTime detectedAt;
}

