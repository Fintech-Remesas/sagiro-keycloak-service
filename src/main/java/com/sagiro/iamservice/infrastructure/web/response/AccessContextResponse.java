package com.sagiro.iamservice.infrastructure.web.response;

import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(description = "Complete access context for the authenticated user, including roles, operational flags, and verification state")
public record AccessContextResponse(
        @Schema(description = "IAM user identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID userId,

        @Schema(description = "Keycloak user identifier (sub claim)", example = "kc-9b2e3f4a-12bc-4def-8765-1a2b3c4d5e6f")
        String keycloakUserId,

        @Schema(description = "JWT subject extracted from the Bearer token", example = "kc-9b2e3f4a-12bc-4def-8765-1a2b3c4d5e6f")
        String subject,

        @Schema(description = "Username of the authenticated user", example = "ana.customer")
        String username,

        @Schema(description = "Email of the authenticated user", example = "ana.customer@techi.test")
        String email,

        @Schema(description = "Set of realm-level roles granted to the user", example = "[\"ROLE_CUSTOMER\"]")
        Set<String> roles,

        @Schema(description = "Current account lifecycle status", example = "ACTIVE")
        AccountStatus accountStatus,

        @Schema(description = "KYC verification status", example = "NOT_STARTED")
        VerificationStatus verificationStatus,

        @Schema(description = "Verification level achieved", example = "NONE")
        VerificationLevel verificationLevel,

        @Schema(description = "Whether the user can perform remittance operations", example = "false")
        boolean canOperate,

        @Schema(description = "Whether the Keycloak identity is enabled", example = "true")
        boolean enabled
) {
}
