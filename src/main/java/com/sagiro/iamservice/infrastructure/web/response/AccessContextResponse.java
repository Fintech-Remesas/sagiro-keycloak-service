package com.sagiro.iamservice.infrastructure.web.response;

import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.domain.enums.VerificationLevel;
import com.sagiro.iamservice.domain.enums.VerificationStatus;

import java.util.Set;
import java.util.UUID;

public record AccessContextResponse(
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
