package com.sagiro.iamservice.infrastructure.web.response;

import com.sagiro.iamservice.domain.enums.CardBrand;
import com.sagiro.iamservice.domain.enums.CardType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardResponse(
    UUID id,
    String cardholderName,
    String last4,
    CardBrand cardBrand,
    CardType cardType,
    int expiryMonth,
    int expiryYear,
    String alias,
    boolean isPrimary,
    LocalDateTime createdAt
) {}
