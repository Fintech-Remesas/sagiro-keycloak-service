package com.sagiro.iamservice.infrastructure.web.request;

import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateVerificationStatusRequest(
        @NotNull VerificationStatus verificationStatus,
        @NotNull VerificationLevel verificationLevel,
        @Schema(example = "2026-04-04T19:30:00Z")
        @NotNull Instant verificationUpdatedAt
) {
}
