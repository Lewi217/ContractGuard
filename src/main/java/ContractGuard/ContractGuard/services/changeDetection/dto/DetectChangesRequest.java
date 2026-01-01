package ContractGuard.ContractGuard.services.changeDetection.dto;

import lombok.*;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectChangesRequest {

    @NotNull(message = "Contract ID is required")
    private UUID contractId;

    @NotNull(message = "Old version is required")
    private String oldVersion;

    @NotNull(message = "New version is required")
    private String newVersion;
}

