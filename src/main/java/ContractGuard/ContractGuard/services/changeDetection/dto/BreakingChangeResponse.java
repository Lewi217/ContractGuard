package ContractGuard.ContractGuard.services.changeDetection.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakingChangeResponse {

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
    private LocalDateTime detectedAt;
}

