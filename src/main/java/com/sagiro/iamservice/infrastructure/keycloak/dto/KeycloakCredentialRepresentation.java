package com.sagiro.iamservice.infrastructure.keycloak.dto;

public record KeycloakCredentialRepresentation(
        String type,
        String value,
        boolean temporary
) {
}
