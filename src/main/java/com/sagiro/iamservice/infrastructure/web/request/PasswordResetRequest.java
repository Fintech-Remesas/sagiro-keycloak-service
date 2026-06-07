package com.sagiro.iamservice.infrastructure.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to complete a password reset using a one-time token received via the communication-service")
public record PasswordResetRequest(
        @Schema(
                description = "The one-time password reset token received via email or SMS",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @NotBlank String token,

        @Schema(
                description = "The new password to set. Must meet Keycloak's password policy.",
                example = "NewSecure#Pass2026"
        )
        @NotBlank @Size(min = 8, max = 120) String newPassword
) {
}
