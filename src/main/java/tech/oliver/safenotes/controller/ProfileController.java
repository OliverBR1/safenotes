package tech.oliver.safenotes.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.oliver.safenotes.controller.dto.ProfileResponse;
import tech.oliver.safenotes.service.UserService;

@RestController
@RequestMapping("/me")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_profile:read')")
    public ResponseEntity<ProfileResponse> profile(@AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(userService.readProfile(jwt));
    }
}
