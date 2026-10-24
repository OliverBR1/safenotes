package tech.oliver.safenotes.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import tech.oliver.safenotes.controller.dto.ProfileResponse;
import tech.oliver.safenotes.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileResponse readProfile(Jwt jwt) {

        var user = userRepository.getReferenceById(Long.valueOf(jwt.getSubject()));

        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getTokenVersion(),
                jwt.getClaimAsStringList("scope"),
                jwt.getClaimAsStringList("roles")
        );
    }
}
