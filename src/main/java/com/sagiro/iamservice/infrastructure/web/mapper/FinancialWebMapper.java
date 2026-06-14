package com.sagiro.iamservice.infrastructure.web.mapper;

import com.sagiro.iamservice.domain.model.BankAccount;
import com.sagiro.iamservice.domain.model.Card;
import com.sagiro.iamservice.infrastructure.web.response.BankAccountResponse;
import com.sagiro.iamservice.infrastructure.web.response.CardResponse;

public class FinancialWebMapper {

    public static CardResponse toResponse(Card card) {
        if (card == null) return null;
        return new CardResponse(
            card.getId(),
            card.getCardholderName(),
            card.getLast4(),
            card.getCardBrand(),
            card.getCardType(),
            card.getExpiryMonth(),
            card.getExpiryYear(),
            card.getAlias(),
            card.isPrimary(),
            card.getCreatedAt()
        );
    }

    public static BankAccountResponse toResponse(BankAccount bankAccount) {
        if (bankAccount == null) return null;
        return new BankAccountResponse(
            bankAccount.getId(),
            bankAccount.getBankName(),
            bankAccount.getLast4(),
            bankAccount.getAccountType(),
            bankAccount.getCurrency(),
            bankAccount.getCountry(),
            bankAccount.getAlias(),
            bankAccount.isPrimary(),
            bankAccount.getCreatedAt()
        );
    }
}
