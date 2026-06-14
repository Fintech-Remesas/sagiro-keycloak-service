package com.sagiro.iamservice.infrastructure.messaging.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sagiro.iamservice.application.port.output.IamEventPublisherPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class KafkaIamEventPublisher implements IamEventPublisherPort {

    private static final String IAM_EVENTS_TOPIC = "iam.events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaIamEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishCardAdded(String userId, UUID cardId, String last4) {
        publishEvent("CARD_ADDED", userId, Map.of(
            "cardId", cardId.toString(),
            "last4", last4
        ));
    }

    @Override
    public void publishCardDeleted(String userId, UUID cardId) {
        publishEvent("CARD_DELETED", userId, Map.of(
            "cardId", cardId.toString()
        ));
    }

    @Override
    public void publishBankAccountAdded(String userId, UUID bankAccountId, String last4) {
        publishEvent("BANK_ACCOUNT_ADDED", userId, Map.of(
            "bankAccountId", bankAccountId.toString(),
            "last4", last4
        ));
    }

    @Override
    public void publishBankAccountDeleted(String userId, UUID bankAccountId) {
        publishEvent("BANK_ACCOUNT_DELETED", userId, Map.of(
            "bankAccountId", bankAccountId.toString()
        ));
    }

    private void publishEvent(String eventType, String userId, Map<String, Object> payload) {
        Map<String, Object> event = Map.of(
            "eventType", eventType,
            "userId", userId,
            "timestamp", java.time.Instant.now().toString(),
            "payload", payload
        );
        
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            // Use userId as the Kafka message key for partition routing guarantees
            kafkaTemplate.send(IAM_EVENTS_TOPIC, userId, jsonPayload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize IAM event", e);
        }
    }
}
