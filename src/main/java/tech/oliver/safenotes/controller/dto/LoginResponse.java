package tech.oliver.safenotes.controller.dto;

import java.util.List;

public record LoginResponse(String accessToken,
                            Integer expiresIn,
                            String refreshToken,
                            Integer refreshExpiresIn,
                            List<String> scopes) {
}
