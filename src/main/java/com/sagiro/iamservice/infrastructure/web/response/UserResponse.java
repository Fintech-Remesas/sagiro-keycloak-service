package com.sagiro.iamservice.infrastructure.web.response;

import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
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
        UserProfileResponse profile
) {
}
