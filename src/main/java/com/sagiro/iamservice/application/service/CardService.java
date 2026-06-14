package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.AddCardCommand;
import com.sagiro.iamservice.application.port.input.ManageCardsUseCase;
import com.sagiro.iamservice.application.port.output.CardRepositoryPort;
import com.sagiro.iamservice.application.port.output.IamEventPublisherPort;
import com.sagiro.iamservice.domain.model.Card;
import com.sagiro.iamservice.application.exception.ConflictException;
import com.sagiro.iamservice.application.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CardService implements ManageCardsUseCase {

    private final CardRepositoryPort cardRepository;
    private final IamEventPublisherPort eventPublisher;

    public CardService(CardRepositoryPort cardRepository, IamEventPublisherPort eventPublisher) {
        this.cardRepository = cardRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Card addCard(AddCardCommand command) {
        String last4 = command.cardNumber().substring(command.cardNumber().length() - 4);
        
        // Idempotency / Duplicate Check
        if (cardRepository.existsByUserIdAndLast4AndCardBrand(command.userId(), last4, command.cardBrand())) {
            throw new ConflictException("Card already exists");
        }

        boolean isPrimary = cardRepository.findPrimaryByUserId(command.userId()).isEmpty();

        Card card = Card.create(
            command.userId(),
            command.cardholderName(),
            last4,
            command.cardBrand(),
            command.cardType(),
            command.expiryMonth(),
            command.expiryYear(),
            command.alias(),
            isPrimary
        );

        Card savedCard = cardRepository.save(card);
        eventPublisher.publishCardAdded(savedCard.getUserId(), savedCard.getId(), savedCard.getLast4());
        
        return savedCard;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> getCards(String userId) {
        return cardRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void setPrimaryCard(String userId, UUID cardId) {
        Card targetCard = cardRepository.findById(cardId)
            .filter(c -> c.getUserId().equals(userId))
            .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        if (targetCard.isPrimary()) {
            return;
        }

        cardRepository.findPrimaryByUserId(userId).ifPresent(primary -> {
            primary.removePrimary();
            cardRepository.save(primary);
        });

        targetCard.markAsPrimary();
        cardRepository.save(targetCard);
    }

    @Override
    @Transactional
    public void deleteCard(String userId, UUID cardId) {
        Card targetCard = cardRepository.findById(cardId)
            .filter(c -> c.getUserId().equals(userId))
            .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        targetCard.delete();
        cardRepository.save(targetCard);
        
        eventPublisher.publishCardDeleted(userId, cardId);

        if (targetCard.isPrimary()) {
            cardRepository.findByUserId(userId).stream()
                .filter(c -> !c.isDeleted() && !c.getId().equals(cardId))
                .findFirst()
                .ifPresent(newPrimary -> {
                    newPrimary.markAsPrimary();
                    cardRepository.save(newPrimary);
                });
        }
    }
}
