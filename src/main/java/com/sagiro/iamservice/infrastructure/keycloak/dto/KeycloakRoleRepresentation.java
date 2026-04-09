package com.sagiro.iamservice.infrastructure.keycloak.dto;

public record KeycloakRoleRepresentation(
        String id,
        String name,
        String description
) {
}
