package com.sagiro.iamservice.infrastructure.web.response;

import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;

import java.time.Instant;
import java.util.UUID;

public record UserStatusResponse(
        UUID userId,
        AccountStatus accountStatus,
        VerificationStatus verificationStatus,
        VerificationLevel verificationLevel,
        boolean canOperate,
        boolean enabled,
        Instant verificationUpdatedAt
) {
}
