package com.sagiro.iamservice.infrastructure.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @Schema(example = "+51911111111")
        @Size(max = 30) String phone,
        @Schema(example = "Ana Maria")
        @NotBlank @Size(max = 80) String firstName,
        @Schema(example = "Torres Vega")
        @NotBlank @Size(max = 80) String lastName,
        @Schema(example = "PE")
        @NotBlank @Size(max = 3) String country,
        @Schema(example = "es")
        @NotBlank @Size(max = 10) String preferredLanguage,
        @Schema(example = "true")
        boolean blockchainVisibilityEnabled,
        @Schema(example = "false")
        boolean darkModeEnabled
) {
}
