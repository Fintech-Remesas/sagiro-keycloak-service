package com.sagiro.iamservice.application.dto;

import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;

import java.time.Instant;
import java.util.UUID;

public record UpdateVerificationStatusCommand(
        UUID userId,
        VerificationStatus verificationStatus,
        VerificationLevel verificationLevel,
        Instant verificationUpdatedAt
) {
}
