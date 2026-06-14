package com.sagiro.iamservice.infrastructure.web.response;

import com.sagiro.iamservice.domain.enums.AccountType;

import java.time.LocalDateTime;
import java.util.UUID;

public record BankAccountResponse(
    UUID id,
    String bankName,
    String last4,
    AccountType accountType,
    String currency,
    String country,
    String alias,
    boolean isPrimary,
    LocalDateTime createdAt
) {}
