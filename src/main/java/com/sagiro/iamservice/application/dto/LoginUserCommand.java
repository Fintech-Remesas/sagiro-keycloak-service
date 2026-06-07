package com.sagiro.iamservice.application.dto;

/**
 * Command for authenticating a user.
 * The usernameOrEmail can be either the username or the email address registered in Keycloak.
 */
public record LoginUserCommand(
        String usernameOrEmail,
        String password
) {
}
