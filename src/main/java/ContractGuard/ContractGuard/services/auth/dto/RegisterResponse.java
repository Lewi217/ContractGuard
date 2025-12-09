package ContractGuard.ContractGuard.services.auth.dto;

import ContractGuard.ContractGuard.services.contract.dto.OrganizationResponse;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private UserResponse user;
    private OrganizationResponse organization;
}

