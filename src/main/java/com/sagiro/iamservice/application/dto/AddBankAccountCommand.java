package com.sagiro.iamservice.application.dto;

import com.sagiro.iamservice.domain.enums.AccountType;

public record AddBankAccountCommand(
    String userId,
    String idempotencyKey,
    String bankName,
    String accountNumber,
    AccountType accountType,
    String currency,
    String country,
    String alias
) {}
