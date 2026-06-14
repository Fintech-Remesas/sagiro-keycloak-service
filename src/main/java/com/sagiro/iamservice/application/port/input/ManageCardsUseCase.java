package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.AddCardCommand;
import com.sagiro.iamservice.domain.model.Card;
import java.util.List;
import java.util.UUID;

public interface ManageCardsUseCase {
    Card addCard(AddCardCommand command);
    List<Card> getCards(String userId);
    void setPrimaryCard(String userId, UUID cardId);
    void deleteCard(String userId, UUID cardId);
}
