package com.sagiro.iamservice.infrastructure.web.response;

import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Condensed IAM status snapshot for a specific user")
public record UserStatusResponse(
        @Schema(description = "IAM user identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID userId,

        @Schema(description = "Current account lifecycle status", example = "ACTIVE",
                allowableValues = {"REGISTERED", "ACTIVE", "SUSPENDED", "BLOCKED"})
        AccountStatus accountStatus,

        @Schema(description = "KYC verification status", example = "VERIFIED",
                allowableValues = {"NOT_STARTED", "IN_PROGRESS", "VERIFIED", "REJECTED"})
        VerificationStatus verificationStatus,

        @Schema(description = "Verification level achieved", example = "BASIC",
                allowableValues = {"NONE", "BASIC", "ENHANCED"})
        VerificationLevel verificationLevel,

        @Schema(description = "Whether the user is authorized to perform remittance operations", example = "true")
        boolean canOperate,

        @Schema(description = "Whether the Keycloak identity is active", example = "true")
        boolean enabled,

        @Schema(description = "Timestamp of the last verification status change (UTC)", example = "2026-06-01T10:00:00Z")
        Instant verificationUpdatedAt,

        @Schema(description = "Whether the user is allowed to perform test recharges (admin-only toggle)", example = "false")
        boolean allowTestRecharge,

        @Schema(description = "Whether the user has already claimed the welcome bonus", example = "false")
        boolean welcomeBonusClaimed
) {
}
