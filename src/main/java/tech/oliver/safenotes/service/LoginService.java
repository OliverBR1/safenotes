package tech.oliver.safenotes.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.oliver.safenotes.config.JwtConfig;
import tech.oliver.safenotes.controller.dto.LoginResponse;
import tech.oliver.safenotes.entity.User;
import tech.oliver.safenotes.repository.UserRepository;

import java.util.UUID;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenService accessTokenService;
    private final JwtConfig jwtConfig;

    public LoginService(UserRepository userRepository,
                        BCryptPasswordEncoder bCryptPasswordEncoder,
                        RefreshTokenService refreshTokenService,
                        AccessTokenService accessTokenService,
                        JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.accessTokenService = accessTokenService;
        this.jwtConfig = jwtConfig;
    }

    @Transactional
    public LoginResponse login(String username, String password) {

        var user = validateUser(username, password);

        var familyId = UUID.randomUUID();

        var opaqueToken = refreshTokenService.generateRefreshToken(user, familyId);

        var accessToken = accessTokenService.generateAccessToken(user, familyId);

        return new LoginResponse(
                accessToken.getTokenValue(),
                jwtConfig.getExpiresIn(),
                opaqueToken,
                jwtConfig.getRefreshExpiresIn(),
                accessToken.getClaimAsStringList("scope")
        );
    }

    private User validateUser(String username, String password) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(RuntimeException::new);

        var isPasswordValid = bCryptPasswordEncoder.matches(password, user.getPassword());
        if (!isPasswordValid) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }
}
