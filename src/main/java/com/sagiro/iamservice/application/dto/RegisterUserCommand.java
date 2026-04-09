package com.sagiro.iamservice.application.dto;

public record RegisterUserCommand(
        String email,
        String username,
        String phone,
        String firstName,
        String lastName,
        String country,
        String preferredLanguage,
        String initialPassword
) {
}
