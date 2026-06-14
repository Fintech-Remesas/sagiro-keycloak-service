package com.sagiro.iamservice.application.dto;

import com.sagiro.iamservice.domain.enums.CardBrand;
import com.sagiro.iamservice.domain.enums.CardType;

public record AddCardCommand(
    String userId,
    String idempotencyKey,
    String cardholderName,
    String cardNumber, // We will extract last4 and discard the rest
    int expiryMonth,
    int expiryYear,
    CardBrand cardBrand,
    CardType cardType,
    String alias
) {}
