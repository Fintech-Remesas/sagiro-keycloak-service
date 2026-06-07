package com.sagiro.iamservice.infrastructure.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "User profile preferences and regional settings")
public record UserProfileResponse(
        @Schema(description = "Profile identifier", example = "7d2b4e8a-1234-4bcd-9876-abcdef012345")
        UUID id,

        @Schema(description = "Foreign key referencing the IAM user", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID userId,

        @Schema(description = "ISO 3166-1 alpha-2 country code of the user's residence", example = "PE")
        String country,

        @Schema(description = "ISO 639-1 preferred language code", example = "es")
        String preferredLanguage,

        @Schema(description = "Whether the user has opted into blockchain transaction visibility", example = "false")
        boolean blockchainVisibilityEnabled,

        @Schema(description = "Whether the user prefers dark mode in the frontend app", example = "true")
        boolean darkModeEnabled
) {
}
