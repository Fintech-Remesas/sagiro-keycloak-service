package com.sagiro.iamservice.domain.model;

import com.sagiro.iamservice.domain.enums.CardBrand;
import com.sagiro.iamservice.domain.enums.CardType;
import java.time.LocalDateTime;
import java.util.UUID;

public class Card {
    private final UUID id;
    private final String userId;
    private final String cardholderName;
    private final String last4;
    private final CardBrand cardBrand;
    private final CardType cardType;
    private final int expiryMonth;
    private final int expiryYear;
    private final String alias;
    private boolean isPrimary;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Card(UUID id, String userId, String cardholderName, String last4, CardBrand cardBrand, CardType cardType, int expiryMonth, int expiryYear, String alias, boolean isPrimary, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        if (last4 == null || last4.length() != 4) {
            throw new IllegalArgumentException("last4 must be exactly 4 characters");
        }
        this.id = id;
        this.userId = userId;
        this.cardholderName = cardholderName;
        this.last4 = last4;
        this.cardBrand = cardBrand;
        this.cardType = cardType;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.alias = alias;
        this.isPrimary = isPrimary;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Card create(String userId, String cardholderName, String last4, CardBrand cardBrand, CardType cardType, int expiryMonth, int expiryYear, String alias, boolean isPrimary) {
        return new Card(UUID.randomUUID(), userId, cardholderName, last4, cardBrand, cardType, expiryMonth, expiryYear, alias, isPrimary, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public void markAsPrimary() {
        this.isPrimary = true;
    }

    public void removePrimary() {
        this.isPrimary = false;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public UUID getId() { return id; }
    public String getUserId() { return userId; }
    public String getCardholderName() { return cardholderName; }
    public String getLast4() { return last4; }
    public CardBrand getCardBrand() { return cardBrand; }
    public CardType getCardType() { return cardType; }
    public int getExpiryMonth() { return expiryMonth; }
    public int getExpiryYear() { return expiryYear; }
    public String getAlias() { return alias; }
    public boolean isPrimary() { return isPrimary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
