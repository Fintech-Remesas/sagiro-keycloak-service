package com.sagiro.iamservice.infrastructure.messaging.kafka.event;

public record CustomerActivatedEvent(
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
