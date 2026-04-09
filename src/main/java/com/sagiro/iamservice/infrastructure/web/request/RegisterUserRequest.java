package com.sagiro.iamservice.infrastructure.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @Schema(example = "ana.customer@techi.test")
        @NotBlank @Email String email,
        @Schema(example = "ana.customer")
        @NotBlank @Size(max = 50) String username,
        @Schema(example = "+51999999999")
        @Size(max = 30) String phone,
        @Schema(example = "Ana")
        @NotBlank @Size(max = 80) String firstName,
        @Schema(example = "Torres")
        @NotBlank @Size(max = 80) String lastName,
        @Schema(example = "PE")
        @NotBlank @Size(max = 3) String country,
        @Schema(example = "es")
        @NotBlank @Size(max = 10) String preferredLanguage,
        @Schema(description = "Optional temporary password forwarded to Keycloak and never stored locally", example = "TempPass#2026")
        @Size(max = 120) String initialPassword
) {
}
