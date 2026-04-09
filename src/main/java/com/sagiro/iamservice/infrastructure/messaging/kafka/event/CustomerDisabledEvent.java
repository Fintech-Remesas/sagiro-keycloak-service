package com.sagiro.iamservice.infrastructure.messaging.kafka.event;

public record CustomerDisabledEvent(
        String keycloakUserId,
        String email,
        String username,
        String reason
) {
}
