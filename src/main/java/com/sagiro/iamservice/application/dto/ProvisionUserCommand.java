package com.sagiro.iamservice.application.dto;

import com.sagiro.iamservice.domain.enums.AccountStatus;

public record ProvisionUserCommand(
        String keycloakUserId,
        String email,
        String username,
        String phone,
        String firstName,
        String lastName,
        String country,
        String preferredLanguage,
        AccountStatus accountStatus,
        boolean enabled
) {
}
