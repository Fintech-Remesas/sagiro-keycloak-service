package com.sagiro.iamservice.infrastructure.messaging.kafka.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sagiro.iamservice.application.dto.DeactivateUserCommand;
import com.sagiro.iamservice.application.dto.ProvisionUserCommand;
import com.sagiro.iamservice.application.port.input.DeactivateUserFromEventUseCase;
import com.sagiro.iamservice.application.port.input.ProvisionUserFromEventUseCase;
import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.infrastructure.messaging.kafka.event.CustomerActivatedEvent;
import com.sagiro.iamservice.infrastructure.messaging.kafka.event.CustomerCreatedEvent;
import com.sagiro.iamservice.infrastructure.messaging.kafka.event.CustomerDisabledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerProvisioningListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerProvisioningListener.class);

    private final ObjectMapper objectMapper;
    private final ProvisionUserFromEventUseCase provisionUserFromEventUseCase;
    private final DeactivateUserFromEventUseCase deactivateUserFromEventUseCase;

    public CustomerProvisioningListener(
            ObjectMapper objectMapper,
            ProvisionUserFromEventUseCase provisionUserFromEventUseCase,
            DeactivateUserFromEventUseCase deactivateUserFromEventUseCase
    ) {
        this.objectMapper = objectMapper;
        this.provisionUserFromEventUseCase = provisionUserFromEventUseCase;
        this.deactivateUserFromEventUseCase = deactivateUserFromEventUseCase;
    }

    @KafkaListener(topics = "${app.kafka.topics.customer-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void onCustomerCreated(String payload) {
        CustomerCreatedEvent event = read(payload, CustomerCreatedEvent.class);
        LOGGER.info("Consuming customer created event for username={}", event.username());
        provisionUserFromEventUseCase.provision(new ProvisionUserCommand(
                event.keycloakUserId(),
                event.email(),
                event.username(),
                event.phone(),
                event.firstName(),
                event.lastName(),
                event.country(),
                event.preferredLanguage(),
                AccountStatus.REGISTERED,
                true
        ));
    }

    @KafkaListener(topics = "${app.kafka.topics.customer-activated}", groupId = "${spring.kafka.consumer.group-id}")
    public void onCustomerActivated(String payload) {
        CustomerActivatedEvent event = read(payload, CustomerActivatedEvent.class);
        LOGGER.info("Consuming customer activated event for username={}", event.username());
        provisionUserFromEventUseCase.provision(new ProvisionUserCommand(
                event.keycloakUserId(),
                event.email(),
                event.username(),
                event.phone(),
                event.firstName(),
                event.lastName(),
                event.country(),
                event.preferredLanguage(),
                AccountStatus.ACTIVE,
                true
        ));
    }

    @KafkaListener(topics = "${app.kafka.topics.customer-disabled}", groupId = "${spring.kafka.consumer.group-id}")
    public void onCustomerDisabled(String payload) {
        CustomerDisabledEvent event = read(payload, CustomerDisabledEvent.class);
        LOGGER.info("Consuming customer disabled event for username={} reason={}", event.username(), event.reason());
        deactivateUserFromEventUseCase.deactivate(new DeactivateUserCommand(
                event.keycloakUserId(),
                event.email(),
                event.username(),
                event.reason()
        ));
    }

    private <T> T read(String payload, Class<T> targetType) {
        try {
            return objectMapper.readValue(payload, targetType);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to deserialize Kafka payload into " + targetType.getSimpleName(), exception);
        }
    }
}
