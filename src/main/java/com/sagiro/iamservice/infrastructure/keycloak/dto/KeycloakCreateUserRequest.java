package com.sagiro.iamservice.infrastructure.keycloak.dto;

import java.util.List;

public record KeycloakCreateUserRequest(
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        List<String> requiredActions,
        List<KeycloakCredentialRepresentation> credentials
) {
}
