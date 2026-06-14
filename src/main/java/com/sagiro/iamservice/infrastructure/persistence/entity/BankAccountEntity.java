package com.sagiro.iamservice.infrastructure.persistence.entity;

import com.sagiro.iamservice.infrastructure.security.encryption.EncryptedAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank_accounts")
public class BankAccountEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Convert(converter = EncryptedAttributeConverter.class)
    @Column(name = "account_number_encrypted", nullable = false, columnDefinition = "text")
    private String accountNumber;

    @Column(name = "last4", nullable = false, length = 4)
    private String last4;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "alias")
    private String alias;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public BankAccountEntity() {}

    public BankAccountEntity(UUID id, String userId, String bankName, String accountNumber, String last4, String accountType, String currency, String country, String alias, boolean isPrimary, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getLast4() { return last4; }
    public void setLast4(String last4) { this.last4 = last4; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
