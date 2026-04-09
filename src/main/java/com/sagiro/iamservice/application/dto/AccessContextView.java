package com.sagiro.iamservice.application.dto;

import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;

import java.util.Set;
import java.util.UUID;

public record AccessContextView(
        UUID userId,
        String keycloakUserId,
        String subject,
        String username,
        String email,
        Set<String> roles,
        AccountStatus accountStatus,
        VerificationStatus verificationStatus,
        VerificationLevel verificationLevel,
        boolean canOperate,
        boolean enabled
) {
}
