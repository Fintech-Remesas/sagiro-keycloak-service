package com.sagiro.iamservice.infrastructure.persistence.mapper;

import com.sagiro.iamservice.domain.enums.CardBrand;
import com.sagiro.iamservice.domain.enums.CardType;
import com.sagiro.iamservice.domain.model.Card;
import com.sagiro.iamservice.infrastructure.persistence.entity.CardEntity;

public class CardPersistenceMapper {

    public static Card toDomain(CardEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Card(
            entity.getId(),
            entity.getUserId(),
            entity.getCardholderName(),
            entity.getLast4(),
            CardBrand.valueOf(entity.getCardBrand()),
            CardType.valueOf(entity.getCardType()),
            entity.getExpiryMonth(),
            entity.getExpiryYear(),
            entity.getAlias(),
            entity.isPrimary(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getDeletedAt()
        );
    }

    public static CardEntity toEntity(Card domain) {
        if (domain == null) {
            return null;
        }
        return new CardEntity(
            domain.getId(),
            domain.getUserId(),
            domain.getCardholderName(),
            domain.getLast4(),
            domain.getCardBrand().name(),
            domain.getCardType().name(),
            domain.getExpiryMonth(),
            domain.getExpiryYear(),
            domain.getAlias(),
            domain.isPrimary(),
            domain.getCreatedAt(),
            domain.getUpdatedAt(),
            domain.getDeletedAt()
        );
    }
}
