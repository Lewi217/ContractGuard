package ContractGuard.ContractGuard.services.contract.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResponse {
    private UUID id;
    private String name;
    private String version;
    private String status;
    private UUID organizationId;
    private String basePath;
    private JsonNode openapiSpec;
    private String blobStorageUrl;
    private String[] tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deprecatedAt;
    private LocalDateTime retiredAt;
}

