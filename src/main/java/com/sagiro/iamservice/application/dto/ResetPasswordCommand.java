package com.sagiro.iamservice.application.dto;

/**
 * Command for resetting a user's password using the one-time token
 * previously generated and sent to the user via the communication-service.
 */
public record ResetPasswordCommand(
        String token,
        String newPassword
) {
}
