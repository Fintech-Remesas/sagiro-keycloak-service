package com.sagiro.iamservice.application.dto;

public record UpdateUserProfileCommand(
        String phone,
        String firstName,
        String lastName,
        String country,
        String preferredLanguage,
        boolean blockchainVisibilityEnabled,
        boolean darkModeEnabled
) {
}
