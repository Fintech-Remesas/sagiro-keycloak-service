package com.sagiro.iamservice.application.dto;

/**
 * Command for initiating a password recovery request.
 * The email is used to look up the IAM user and generate a one-time reset token.
 */
public record RequestPasswordRecoveryCommand(
        String email
) {
}
