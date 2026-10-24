package tech.oliver.safenotes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.oliver.safenotes.config.JwtConfig;
import tech.oliver.safenotes.config.OpaqueToken;
import tech.oliver.safenotes.controller.dto.LoginResponse;
import tech.oliver.safenotes.entity.RefreshToken;
import tech.oliver.safenotes.entity.User;
import tech.oliver.safenotes.repository.RefreshTokenRepository;
import tech.oliver.safenotes.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;
    private final AccessTokenService accessTokenService;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               JwtConfig jwtConfig, AccessTokenService accessTokenService, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtConfig = jwtConfig;
        this.accessTokenService = accessTokenService;
        this.userRepository = userRepository;
    }

    @Transactional
    public LoginResponse refreshToken(String opaqueToken) {

        var opaqueHash = OpaqueToken.generateOpaqueHash(opaqueToken);
        var refreshToken = getRefreshToken(opaqueHash);

        if (isExpired(refreshToken)) {
            throw new RuntimeException("Refresh token expired");
        }

        if (refreshToken.getRevokedAt() != null) {
            logger.warn("Refresh token already revoked - security issue - revoking token family...");
            revokeTokenFamily(refreshToken.getFamilyId());
            throw new RuntimeException("Refresh token already revoked");
        }

        revokeCurrentRefreshToken(refreshToken);

        var user = userRepository.getReferenceById(refreshToken.getUser().getId());

        var accessToken = accessTokenService.generateAccessToken(user, refreshToken.getFamilyId());

        var newRefreshToken = this.generateRefreshToken(user, refreshToken.getFamilyId());

        return new LoginResponse(
                accessToken.getTokenValue(),
                jwtConfig.getExpiresIn(),
                newRefreshToken,
                jwtConfig.getRefreshExpiresIn(),
                accessToken.getClaimAsStringList("scope")
        );
    }

    private void revokeCurrentRefreshToken(RefreshToken refreshToken) {
        var rowsUpdated = refreshTokenRepository.revokeIfNotRevoked(Instant.now(), refreshToken.getOpaqueHash());
        if (rowsUpdated == 0) {
            logger.warn("RaceCondition: Refresh token already revoked - security issue - revoking token family...");
            revokeTokenFamily(refreshToken.getFamilyId());
            throw new RuntimeException("RaceCondition: Refresh token already revoked");
        }
    }

    private void revokeTokenFamily(UUID familyId) {
        refreshTokenRepository.updatedRevokedAtByFamilyId(Instant.now(), familyId);
    }

    private RefreshToken getRefreshToken(String opaqueHash) {
        var refreshToken = refreshTokenRepository.findByOpaqueHash(opaqueHash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        return refreshToken;
    }

    private static boolean isExpired(RefreshToken refreshToken) {
        return Instant.now().isAfter(refreshToken.getExpiresAt());
    }

    @Transactional
    public String generateRefreshToken(User user, UUID familyId) {

        var opaqueToken = OpaqueToken.generate();
        var opaqueHash = OpaqueToken.generateOpaqueHash(opaqueToken);

        refreshTokenRepository.save(
                new RefreshToken(
                        familyId,
                        opaqueHash,
                        user,
                        Instant.now(),
                        Instant.now().plusSeconds(jwtConfig.getRefreshExpiresIn())
                )
        );

        return opaqueToken;
    }

}
