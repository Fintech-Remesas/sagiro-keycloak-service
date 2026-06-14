package com.sagiro.iamservice.infrastructure.persistence.adapter;

import com.sagiro.iamservice.application.port.output.CardRepositoryPort;
import com.sagiro.iamservice.domain.enums.CardBrand;
import com.sagiro.iamservice.domain.model.Card;
import com.sagiro.iamservice.infrastructure.persistence.mapper.CardPersistenceMapper;
import com.sagiro.iamservice.infrastructure.persistence.repository.JpaCardRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CardPersistenceAdapter implements CardRepositoryPort {

    private final JpaCardRepository repository;

    public CardPersistenceAdapter(JpaCardRepository repository) {
        this.repository = repository;
    }

    @Override
    public Card save(Card card) {
        return CardPersistenceMapper.toDomain(repository.save(CardPersistenceMapper.toEntity(card)));
    }

    @Override
    public Optional<Card> findById(UUID id) {
        return repository.findById(id).map(CardPersistenceMapper::toDomain);
    }

    @Override
    public List<Card> findByUserId(String userId) {
        return repository.findByUserIdAndDeletedAtIsNull(userId).stream()
                .map(CardPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIdAndLast4AndCardBrand(String userId, String last4, CardBrand cardBrand) {
        return repository.existsByUserIdAndLast4AndCardBrandAndDeletedAtIsNull(userId, last4, cardBrand.name());
    }

    @Override
    public Optional<Card> findPrimaryByUserId(String userId) {
        return repository.findByUserIdAndIsPrimaryTrueAndDeletedAtIsNull(userId)
                .map(CardPersistenceMapper::toDomain);
    }
}
