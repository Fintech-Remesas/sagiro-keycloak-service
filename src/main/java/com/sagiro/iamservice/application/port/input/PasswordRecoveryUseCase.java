package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.RequestPasswordRecoveryCommand;
import com.sagiro.iamservice.application.dto.ResetPasswordCommand;

/**
 * Use case for password recovery flows.
 * Decoupled into two distinct operations: initiation and completion.
 */
public interface PasswordRecoveryUseCase {

    /**
     * Initiates the password recovery flow.
     * Generates a secure one-time token, stores it in the user record with a TTL,
     * and publishes a PasswordRecoveryEvent to Kafka for the communication-service.
     *
     * @param command the command containing the user's email address
     * @throws com.sagiro.iamservice.application.exception.ResourceNotFoundException if no user is found with the given email
     */
    void requestRecovery(RequestPasswordRecoveryCommand command);

    /**
     * Completes the password recovery flow by applying the new password.
     * Validates the token has not expired, resets the password in Keycloak via the admin API,
     * then clears the token from the user record.
     *
     * @param command the command containing the token and the new password
     * @throws com.sagiro.iamservice.application.exception.ResourceNotFoundException if the token does not match any user
     * @throws com.sagiro.iamservice.application.exception.ConflictException          if the token has expired
     */
    void resetPassword(ResetPasswordCommand command);
}
