package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.LoginUserCommand;
import com.sagiro.iamservice.application.dto.LoginView;
import com.sagiro.iamservice.application.port.input.LoginUseCase;
import com.sagiro.iamservice.application.port.output.KeycloakAdminPort;

/**
 * Application service that authenticates a user via Keycloak's token endpoint.
 * Fintech rule: no @Transactional here – no DB write occurs during login,
 * and external HTTP calls (Keycloak) must NOT be wrapped in a database transaction.
 */
public class LoginService implements LoginUseCase {

    private final KeycloakAdminPort keycloakAdminPort;

    public LoginService(KeycloakAdminPort keycloakAdminPort) {
        this.keycloakAdminPort = keycloakAdminPort;
    }

    @Override
    public LoginView login(LoginUserCommand command) {
        return keycloakAdminPort.loginUser(command.usernameOrEmail(), command.password());
    }
}
