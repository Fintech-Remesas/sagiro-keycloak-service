package com.sagiro.iamservice.infrastructure.web.request;

import com.sagiro.iamservice.domain.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddBankAccountRequest(
    @NotBlank String bankName,
    @NotBlank String accountNumber,
    @NotNull AccountType accountType,
    @NotBlank String currency,
    @NotBlank String country,
    String alias
) {}
