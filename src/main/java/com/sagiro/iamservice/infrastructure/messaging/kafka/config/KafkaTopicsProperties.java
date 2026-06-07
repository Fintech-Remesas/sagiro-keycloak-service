package com.sagiro.iamservice.infrastructure.messaging.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProperties(
        String customerCreated,
        String customerActivated,
        String customerDisabled,
        String passwordRecoveryRequested
) {
}
