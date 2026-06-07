package com.sagiro.iamservice.application.dto;

/**
 * Application-layer view representing a successful login result from Keycloak.
 * Carries the OIDC token pair so no infrastructure types leak into the application layer.
 */
public record LoginView(
        String accessToken,
        String refreshToken,
        Long expiresIn,
        Long refreshExpiresIn,
        String tokenType
) {
}
