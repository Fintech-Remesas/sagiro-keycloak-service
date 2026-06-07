package com.sagiro.iamservice.application.event;

import java.time.Instant;

/**
 * Application event representing a request for password recovery.
 * This event is published to Kafka so that the future communication-service
 * can consume it and send the reset token to the user via email or SMS.
 *
 * Security rule: the token MUST be transmitted via secure channel only.
 * Do NOT log the token value in plain text.
 */
public record PasswordRecoveryEvent(
        String userId,
        String email,
        String username,
        String resetToken,
        Instant tokenExpiresAt,
        Instant requestedAt
) {
}
