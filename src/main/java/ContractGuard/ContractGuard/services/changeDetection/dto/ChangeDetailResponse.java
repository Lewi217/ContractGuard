package ContractGuard.ContractGuard.services.changeDetection.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeDetailResponse {
    private UUID id;
    private UUID breakingChangeId;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String changeType;
    private String severity;
    private LocalDateTime createdAt;
}

