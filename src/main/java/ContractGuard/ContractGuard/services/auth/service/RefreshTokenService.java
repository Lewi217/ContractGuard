package ContractGuard.ContractGuard.services.auth.service;

import java.util.UUID;

public interface RefreshTokenService {
    String createRefreshToken(UUID userId, String email);
    UUID validateRefreshToken(String token);
    void deleteRefreshToken(String token);
}
