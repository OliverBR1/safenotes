package tech.oliver.safenotes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.oliver.safenotes.controller.dto.LoginRequest;
import tech.oliver.safenotes.controller.dto.LoginResponse;
import tech.oliver.safenotes.controller.dto.LogoutRequest;
import tech.oliver.safenotes.controller.dto.RefreshRequest;
import tech.oliver.safenotes.service.LoginService;
import tech.oliver.safenotes.service.LogoutService;
import tech.oliver.safenotes.service.RefreshTokenService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginService loginService;
    private final RefreshTokenService refreshTokenService;
    private final LogoutService logoutService;

    public AuthController(LoginService loginService,
                          RefreshTokenService refreshTokenService,
                          LogoutService logoutService) {
        this.loginService = loginService;
        this.refreshTokenService = refreshTokenService;
        this.logoutService = logoutService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {

        var resp = loginService.login(req.username(), req.password());

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> login(@RequestBody RefreshRequest req) {

        var resp = refreshTokenService.refreshToken(req.refreshToken());

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> login(@AuthenticationPrincipal Jwt jwt,
                                               @RequestBody LogoutRequest req) {

        logoutService.logout(Long.parseLong(jwt.getSubject()), req.refreshToken());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<LoginResponse> loginAllDevices(@AuthenticationPrincipal Jwt jwt) {

        logoutService.logoutAll(Long.parseLong(jwt.getSubject()));

        return ResponseEntity.noContent().build();
    }
}
