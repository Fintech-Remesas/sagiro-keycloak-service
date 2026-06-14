package com.sagiro.iamservice.infrastructure.persistence.mapper;

import com.sagiro.iamservice.domain.enums.AccountType;
import com.sagiro.iamservice.domain.model.BankAccount;
import com.sagiro.iamservice.infrastructure.persistence.entity.BankAccountEntity;

public class BankAccountPersistenceMapper {

    public static BankAccount toDomain(BankAccountEntity entity) {
        if (entity == null) {
            return null;
        }
        return new BankAccount(
            entity.getId(),
            entity.getUserId(),
            entity.getBankName(),
            entity.getAccountNumber(), // the entity will provide decrypted via AttributeConverter
            entity.getLast4(),
            AccountType.valueOf(entity.getAccountType()),
            entity.getCurrency(),
            entity.getCountry(),
            entity.getAlias(),
            entity.isPrimary(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getDeletedAt()
        );
    }

    public static BankAccountEntity toEntity(BankAccount domain) {
        if (domain == null) {
            return null;
        }
        return new BankAccountEntity(
            domain.getId(),
            domain.getUserId(),
            domain.getBankName(),
            domain.getAccountNumber(), // will be encrypted via AttributeConverter
            domain.getLast4(),
            domain.getAccountType().name(),
            domain.getCurrency(),
            domain.getCountry(),
            domain.getAlias(),
            domain.isPrimary(),
            domain.getCreatedAt(),
            domain.getUpdatedAt(),
            domain.getDeletedAt()
        );
    }
}
