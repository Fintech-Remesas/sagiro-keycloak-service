package com.sagiro.iamservice.infrastructure.messaging.kafka.event;

public record CustomerCreatedEvent(
        String keycloakUserId,
        String email,
        String username,
        String phone,
        String firstName,
        String lastName,
        String country,
        String preferredLanguage
) {
}
