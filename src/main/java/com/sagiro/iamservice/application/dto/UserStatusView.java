package com.sagiro.iamservice.application.dto;

import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;

import java.time.Instant;
import java.util.UUID;

public record UserStatusView(
        UUID userId,
        AccountStatus accountStatus,
        VerificationStatus verificationStatus,
        VerificationLevel verificationLevel,
        boolean canOperate,
        boolean enabled,
        Instant verificationUpdatedAt,
        boolean allowTestRecharge,
        boolean welcomeBonusClaimed
) {
}
