package com.sagiro.iamservice.infrastructure.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to initiate a password recovery flow for an existing account")
public record PasswordRecoveryRequest(
        @Schema(
                description = "The email address associated with the account to recover",
                example = "ana.customer@techi.test"
        )
        @NotBlank @Email String email
) {
}
