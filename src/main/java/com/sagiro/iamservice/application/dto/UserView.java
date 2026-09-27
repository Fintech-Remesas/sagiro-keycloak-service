package com.sagiro.iamservice.application.dto;

import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;

import java.time.Instant;
import java.util.UUID;

public record UserView(
        UUID id,
        String keycloakUserId,
        String email,
        String username,
        String phone,
        String firstName,
        String lastName,
        AccountStatus accountStatus,
        VerificationStatus verificationStatus,
        VerificationLevel verificationLevel,
        Instant verificationUpdatedAt,
        boolean canOperate,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt,
        boolean allowTestRecharge,
        boolean welcomeBonusClaimed,
        UserProfileView profile
) {
}
