package com.sagiro.iamservice.application.dto;

public record DeactivateUserCommand(
        String keycloakUserId,
        String email,
        String username,
        String reason
) {
}
