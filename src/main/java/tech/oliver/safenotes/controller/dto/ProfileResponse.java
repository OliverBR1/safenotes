package tech.oliver.safenotes.controller.dto;

import java.util.List;

public record ProfileResponse(Long userId,
                              String username,
                              Integer tokenVersion,
                              List<String> scopes,
                              List<String> roles) {
}