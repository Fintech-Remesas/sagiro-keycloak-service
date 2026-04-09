package com.sagiro.iamservice.application.dto;

import java.util.UUID;

public record UserProfileView(
        UUID id,
        UUID userId,
        String country,
        String preferredLanguage,
        boolean blockchainVisibilityEnabled,
        boolean darkModeEnabled
) {
}
