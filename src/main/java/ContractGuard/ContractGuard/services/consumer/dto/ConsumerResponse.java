package ContractGuard.ContractGuard.services.consumer.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumerResponse {
    private UUID id;
    private UUID organizationId;
    private String name;
    private String description;
    private String apiKey;
    private String contactEmail;
    private String contactName;
    private String consumerType;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

