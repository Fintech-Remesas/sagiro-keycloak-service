package com.sagiro.iamservice.infrastructure.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credentials for authenticating a user via Keycloak")
public record LoginRequest(
        @Schema(
                description = "Username or email address registered in the system",
                example = "ana.customer@techi.test"
        )
        @NotBlank @Size(max = 120) String usernameOrEmail,

        @Schema(
                description = "Account password. Must match the password set during registration.",
                example = "TempPass#2026"
        )
        @NotBlank @Size(max = 120) String password
) {
}
