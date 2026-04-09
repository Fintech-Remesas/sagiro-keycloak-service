package com.sagiro.iamservice.infrastructure.keycloak.dto;

public record KeycloakUserRepresentation(
        String id,
        String username,
        String email,
        boolean enabled
) {
}
