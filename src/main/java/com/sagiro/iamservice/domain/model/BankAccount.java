package com.sagiro.iamservice.domain.model;

import com.sagiro.iamservice.domain.enums.AccountType;
import java.time.LocalDateTime;
import java.util.UUID;

public class BankAccount {
    private final UUID id;
    private final String userId;
    private final String bankName;
    private final String accountNumber; // The raw number in domain, gets encrypted at persistence
    private final String last4;
    private final AccountType accountType;
    private final String currency;
    private final String country;
    private final String alias;
    private boolean isPrimary;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public BankAccount(UUID id, String userId, String bankName, String accountNumber, String last4, AccountType accountType, String currency, String country, String alias, boolean isPrimary, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        if (last4 == null || last4.length() != 4) {
            throw new IllegalArgumentException("last4 must be exactly 4 characters");
        }
        this.id = id;
        this.userId = userId;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.last4 = last4;
        this.accountType = accountType;
        this.currency = currency;
        this.country = country;
        this.alias = alias;
        this.isPrimary = isPrimary;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static BankAccount create(String userId, String bankName, String accountNumber, AccountType accountType, String currency, String country, String alias, boolean isPrimary) {
        String last4 = extractLast4(accountNumber);
        return new BankAccount(UUID.randomUUID(), userId, bankName, accountNumber, last4, accountType, currency, country, alias, isPrimary, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    private static String extractLast4(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            throw new IllegalArgumentException("accountNumber must be at least 4 characters long");
        }
        return accountNumber.substring(accountNumber.length() - 4);
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
    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public String getLast4() { return last4; }
    public AccountType getAccountType() { return accountType; }
    public String getCurrency() { return currency; }
    public String getCountry() { return country; }
    public String getAlias() { return alias; }
    public boolean isPrimary() { return isPrimary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
