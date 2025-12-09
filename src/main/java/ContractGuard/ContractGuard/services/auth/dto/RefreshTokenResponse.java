package ContractGuard.ContractGuard.services.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenResponse {
    private String token;
    private String refreshToken;
}