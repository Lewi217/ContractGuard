package ContractGuard.ContractGuard.services.auth.service.impl;

import ContractGuard.ContractGuard.configs.security.JwtProvider;
import ContractGuard.ContractGuard.services.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redis;

    private static final String REFRESH_PREFIX = "refresh:";

    @Override
    public String createRefreshToken(UUID userId, String email) {
        String token = jwtProvider.generateRefreshToken(userId, email);

        redis.opsForValue().set(
                REFRESH_PREFIX + token,
                userId.toString(),
                Duration.ofDays(7)
        );

        return token;
    }

    @Override
    public UUID validateRefreshToken(String token) {
        String key = REFRESH_PREFIX + token;
        String userId = redis.opsForValue().get(key);

        if (userId == null) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        if (!jwtProvider.validateToken(token)) {
            redis.delete(key);
            throw new RuntimeException("Invalid refresh token");
        }

        return UUID.fromString(userId);
    }

    @Override
    public void deleteRefreshToken(String token) {
        redis.delete(REFRESH_PREFIX + token);
    }
}
