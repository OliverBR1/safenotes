package tech.oliver.safenotes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.oliver.safenotes.config.OpaqueToken;
import tech.oliver.safenotes.repository.RefreshTokenRepository;
import tech.oliver.safenotes.repository.UserRepository;

import java.time.Instant;

@Service
public class LogoutService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public LogoutService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public void logout(Long userId, String opaqueToken) {

        var opaqueHash = OpaqueToken.generateOpaqueHash(opaqueToken);

        var isRefreshFromUser = refreshTokenRepository.existsByOpaqueHashAndUserId(opaqueHash, userId);

        if (!isRefreshFromUser) {
            throw new RuntimeException("Invalid refresh token");
        }

        // inserir o access token atual em um blocklist por 15min - table / redis / cache

        refreshTokenRepository.revokeIfNotRevoked(Instant.now(), opaqueHash);
    }

    @Transactional
    public void logoutAll(Long userId) {

        userRepository.incrementTokenVersion(userId);
        refreshTokenRepository.revokeAllFromUserId(Instant.now(), userId);
    }
}
