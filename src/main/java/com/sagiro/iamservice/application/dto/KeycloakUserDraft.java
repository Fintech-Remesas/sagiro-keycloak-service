package com.sagiro.iamservice.application.dto;

import java.util.Set;

public record KeycloakUserDraft(
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        String temporaryPassword,
        Set<String> realmRoles
) {
}
