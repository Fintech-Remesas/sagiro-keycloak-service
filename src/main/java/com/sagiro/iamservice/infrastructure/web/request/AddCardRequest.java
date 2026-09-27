package com.sagiro.iamservice.infrastructure.web.request;

import com.sagiro.iamservice.domain.enums.CardBrand;
import com.sagiro.iamservice.domain.enums.CardType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AddCardRequest(
    @NotBlank String cardholderName,
    @NotBlank @Pattern(regexp = "^\\d{13,19}$", message = "Must be a valid card number") String cardNumber,
    @Min(1) @Max(12) int expiryMonth,
    @Min(2020) int expiryYear,
    @NotNull CardBrand cardBrand,
    @NotNull CardType cardType,
    String alias
) {}
