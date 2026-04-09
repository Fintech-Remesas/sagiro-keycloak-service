package com.sagiro.iamservice.infrastructure.web.response;

import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        UUID userId,
        String country,
        String preferredLanguage,
        boolean blockchainVisibilityEnabled,
        boolean darkModeEnabled
) {
}
