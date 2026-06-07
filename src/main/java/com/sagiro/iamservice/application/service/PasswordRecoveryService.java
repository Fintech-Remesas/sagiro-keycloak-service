package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.RequestPasswordRecoveryCommand;
import com.sagiro.iamservice.application.dto.ResetPasswordCommand;
import com.sagiro.iamservice.application.event.PasswordRecoveryEvent;
import com.sagiro.iamservice.application.exception.ConflictException;
import com.sagiro.iamservice.application.exception.ResourceNotFoundException;
import com.sagiro.iamservice.application.port.input.PasswordRecoveryUseCase;
import com.sagiro.iamservice.application.port.output.KeycloakAdminPort;
import com.sagiro.iamservice.application.port.output.PasswordRecoveryPublisherPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Application service that orchestrates the password recovery flow:
 *
 * Step 1 – requestRecovery:
 *   1. Look up the user by email.
 *   2. Generate a UUID-based one-time token and calculate expiry (now + TTL).
 *   3. Persist the token on the user record (the IAM database owns the token lifecycle).
 *   4. Publish a PasswordRecoveryEvent to Kafka (the communication-service will send the email/SMS).
 *
 * Step 2 – resetPassword:
 *   1. Look up the user by the one-time token.
 *   2. Validate the token has not expired (fintech rule: reject expired tokens immediately).
 *   3. Reset the password in Keycloak via the admin API.
 *   4. Clear the token from the local IAM record.
 *
 * Fintech rules applied:
 * - The Keycloak HTTP call in resetPassword is performed BEFORE clearing the local token
 *   to ensure we do not clear a valid token if Keycloak rejects the call.
 * - Token generation uses UUID.randomUUID() which provides 122 bits of entropy, adequate
 *   for short-lived single-use tokens.
 * - The token is NOT logged in plain text (audit logging redaction rule).
 */
public class PasswordRecoveryService implements PasswordRecoveryUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final KeycloakAdminPort keycloakAdminPort;
    private final PasswordRecoveryPublisherPort publisherPort;
    private final Clock clock;
    private final long tokenTtlMinutes;

    public PasswordRecoveryService(
            UserRepositoryPort userRepositoryPort,
            KeycloakAdminPort keycloakAdminPort,
            PasswordRecoveryPublisherPort publisherPort,
            Clock clock,
            long tokenTtlMinutes
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.keycloakAdminPort = keycloakAdminPort;
        this.publisherPort = publisherPort;
        this.clock = clock;
        this.tokenTtlMinutes = tokenTtlMinutes;
    }

    @Override
    public void requestRecovery(RequestPasswordRecoveryCommand command) {
        User user = userRepositoryPort.findByEmail(command.email())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No IAM user found with email: " + command.email()));

        Instant now = Instant.now(clock);
        Instant expiresAt = now.plusSeconds(tokenTtlMinutes * 60);
        String token = UUID.randomUUID().toString();

        user.requestPasswordReset(token, expiresAt, now);
        userRepositoryPort.save(user);

        // Publish to Kafka – communication-service will deliver the token to the user.
        // Security: we publish the plain token only via Kafka (internal channel), never via HTTP response.
        publisherPort.publish(new PasswordRecoveryEvent(
                user.getId().toString(),
                user.getEmail(),
                user.getUsername(),
                token,
                expiresAt,
                now
        ));
    }

    @Override
    public void resetPassword(ResetPasswordCommand command) {
        User user = userRepositoryPort.findByPasswordResetToken(command.token())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No pending password recovery found for the provided token"));

        Instant now = Instant.now(clock);

        // Fintech rule: reject expired tokens before making any external calls
        if (user.getPasswordResetTokenExpiresAt() == null
                || now.isAfter(user.getPasswordResetTokenExpiresAt())) {
            throw new ConflictException("The password reset token has expired. Please request a new one.");
        }

        // Reset in Keycloak first – if it fails, the token remains valid for retry
        keycloakAdminPort.resetPassword(user.getKeycloakUserId(), command.newPassword());

        // Only clear the token once Keycloak has confirmed the reset
        user.clearPasswordResetToken(now);
        userRepositoryPort.save(user);
    }
}
