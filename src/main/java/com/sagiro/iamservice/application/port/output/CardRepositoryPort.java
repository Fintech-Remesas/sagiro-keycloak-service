package com.sagiro.iamservice.application.port.output;

import com.sagiro.iamservice.domain.model.Card;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepositoryPort {
    Card save(Card card);
    Optional<Card> findById(UUID id);
    List<Card> findByUserId(String userId);
    boolean existsByUserIdAndLast4AndCardBrand(String userId, String last4, com.sagiro.iamservice.domain.enums.CardBrand cardBrand);
    Optional<Card> findPrimaryByUserId(String userId);
}
