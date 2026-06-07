package com.sagiro.iamservice.infrastructure.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OIDC token pair returned after successful authentication via Keycloak")
public record LoginResponse(
        @Schema(
                description = "JWT access token to be included as Bearer token in subsequent requests",
                example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String accessToken,

        @Schema(
                description = "JWT refresh token used to obtain a new access token when it expires",
                example = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ..."
        )
        String refreshToken,

        @Schema(
                description = "Number of seconds until the access token expires",
                example = "300"
        )
        Long expiresIn,

        @Schema(
                description = "Number of seconds until the refresh token expires",
                example = "1800"
        )
        Long refreshExpiresIn,

        @Schema(
                description = "Token type, always 'Bearer' for standard OIDC flows",
                example = "Bearer"
        )
        String tokenType
) {
}
