package com.sagiro.iamservice.infrastructure.web.controller;

import com.sagiro.iamservice.application.dto.AddCardCommand;
import com.sagiro.iamservice.application.port.input.GetCurrentUserUseCase;
import com.sagiro.iamservice.application.port.input.ManageCardsUseCase;
import com.sagiro.iamservice.infrastructure.shared.ApiResponse;
import com.sagiro.iamservice.infrastructure.web.mapper.FinancialWebMapper;
import com.sagiro.iamservice.infrastructure.web.request.AddCardRequest;
import com.sagiro.iamservice.infrastructure.web.response.CardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users/me/cards")
@Tag(name = "Cards", description = "Endpoints for managing user funding cards")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final ManageCardsUseCase manageCardsUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public CardController(ManageCardsUseCase manageCardsUseCase, GetCurrentUserUseCase getCurrentUserUseCase) {
        this.manageCardsUseCase = manageCardsUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @PostMapping
    @Operation(summary = "Add a new card", description = "Requires X-Idempotency-Key header. Only last 4 digits are stored.")
    public ResponseEntity<ApiResponse<CardResponse>> addCard(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody AddCardRequest request) {

        String userId = getCurrentUserUseCase.getCurrentUser().id().toString();

        AddCardCommand command = new AddCardCommand(
            userId,
            idempotencyKey,
            request.cardholderName(),
            request.cardNumber(),
            request.expiryMonth(),
            request.expiryYear(),
            request.cardBrand(),
            request.cardType(),
            request.alias()
        );

        CardResponse response = FinancialWebMapper.toResponse(manageCardsUseCase.addCard(command));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Card added successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get user cards")
    public ResponseEntity<ApiResponse<List<CardResponse>>> getCards() {
        String userId = getCurrentUserUseCase.getCurrentUser().id().toString();
        List<CardResponse> cards = manageCardsUseCase.getCards(userId).stream()
                .map(FinancialWebMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Cards retrieved", cards));
    }

    @PatchMapping("/{cardId}/primary")
    @Operation(summary = "Set card as primary")
    public ResponseEntity<ApiResponse<Void>> setPrimaryCard(@PathVariable UUID cardId) {
        String userId = getCurrentUserUseCase.getCurrentUser().id().toString();
        manageCardsUseCase.setPrimaryCard(userId, cardId);
        return ResponseEntity.ok(ApiResponse.success("Card set as primary", null));
    }

    @DeleteMapping("/{cardId}")
    @Operation(summary = "Delete a card", description = "Soft delete.")
    public ResponseEntity<ApiResponse<Void>> deleteCard(@PathVariable UUID cardId) {
        String userId = getCurrentUserUseCase.getCurrentUser().id().toString();
        manageCardsUseCase.deleteCard(userId, cardId);
        return ResponseEntity.ok(ApiResponse.success("Card deleted successfully", null));
    }
}
