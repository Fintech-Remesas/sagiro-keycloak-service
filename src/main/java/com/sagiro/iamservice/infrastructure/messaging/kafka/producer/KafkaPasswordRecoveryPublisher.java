package com.sagiro.iamservice.infrastructure.messaging.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sagiro.iamservice.application.event.PasswordRecoveryEvent;
import com.sagiro.iamservice.application.port.output.PasswordRecoveryPublisherPort;
import com.sagiro.iamservice.infrastructure.messaging.kafka.config.KafkaTopicsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka-backed implementation of PasswordRecoveryPublisherPort.
 *
 * Kafka standards applied (spring-microservices-kafka skill):
 * - Uses a deterministic partition key (userId) so that all events for the same user
 *   land on the same partition, ensuring ordered processing in the consumer.
 * - The KafkaTemplate is configured with acks=all and enable.idempotence=true (see application.yml).
 * - Security: the token value is present in the event payload but is NOT logged.
 */
@Component
public class KafkaPasswordRecoveryPublisher implements PasswordRecoveryPublisherPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPasswordRecoveryPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaTopicsProperties topics;

    public KafkaPasswordRecoveryPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            KafkaTopicsProperties topics
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topics = topics;
    }

    @Override
    public void publish(PasswordRecoveryEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            // Key = userId ensures all events for the same user go to the same partition (ordered delivery)
            kafkaTemplate.send(topics.passwordRecoveryRequested(), event.userId(), payload);
            LOGGER.info("Published password-recovery event for userId={} email=[REDACTED]", event.userId());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize PasswordRecoveryEvent for userId=" + event.userId(), exception);
        }
    }
}
