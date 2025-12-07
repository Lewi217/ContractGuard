package ContractGuard.ContractGuard.services.contract.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String plan;
}

