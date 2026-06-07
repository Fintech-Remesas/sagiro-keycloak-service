package com.sagiro.iamservice.infrastructure.web.response;

import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Full IAM user representation including identity, state, and profile preferences")
public record UserResponse(
        @Schema(description = "Internal IAM user identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Keycloak user ID (sub claim from the JWT)", example = "kc-9b2e3f4a-12bc-4def-8765-1a2b3c4d5e6f")
        String keycloakUserId,

        @Schema(description = "User's email address", example = "ana.customer@techi.test")
        String email,

        @Schema(description = "Username for login", example = "ana.customer")
        String username,

        @Schema(description = "Phone number in E.164 format", example = "+51999999999")
        String phone,

        @Schema(description = "User's first name", example = "Ana")
        String firstName,

        @Schema(description = "User's last name", example = "Torres")
        String lastName,

        @Schema(description = "Current account lifecycle status", example = "ACTIVE")
        AccountStatus accountStatus,

        @Schema(description = "KYC verification status", example = "NOT_STARTED")
        VerificationStatus verificationStatus,

        @Schema(description = "Current verification level achieved", example = "NONE")
        VerificationLevel verificationLevel,

        @Schema(description = "Timestamp of the last verification status update (UTC)", example = "2026-06-01T10:00:00Z")
        Instant verificationUpdatedAt,

        @Schema(description = "Whether the user can perform operations (derived from account and verification state)", example = "true")
        boolean canOperate,

        @Schema(description = "Whether the Keycloak account is enabled", example = "true")
        boolean enabled,

        @Schema(description = "Account creation timestamp (UTC)", example = "2026-05-20T08:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp (UTC)", example = "2026-06-05T14:00:00Z")
        Instant updatedAt,

        @Schema(description = "User's profile preferences")
        UserProfileResponse profile
) {
}
