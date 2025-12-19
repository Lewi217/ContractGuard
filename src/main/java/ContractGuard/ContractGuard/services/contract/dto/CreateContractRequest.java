package ContractGuard.ContractGuard.services.contract.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateContractRequest {

    @NotBlank(message = "Contract name is required")
    private String name;

    @NotBlank(message = "Version is required")
    private String version;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @NotBlank(message = "Base path is required")
    private String basePath;

    @NotBlank(message = "OpenAPI URL is required")
    private String openapiUrl;

    private String description;
    private String[] tags;
}

