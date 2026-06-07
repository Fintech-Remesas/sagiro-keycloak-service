package com.sagiro.iamservice.application.port.input;

import com.sagiro.iamservice.application.dto.LoginUserCommand;
import com.sagiro.iamservice.application.dto.LoginView;

/**
 * Use case for authenticating a user through Keycloak.
 * Returns an OIDC token pair (access + refresh) on success.
 */
public interface LoginUseCase {

    /**
     * Authenticates the user via Keycloak's token endpoint.
     *
     * @param command the login credentials (usernameOrEmail + password)
     * @return an OIDC token pair from Keycloak
     * @throws com.sagiro.iamservice.application.exception.UnauthorizedException if credentials are invalid
     */
    LoginView login(LoginUserCommand command);
}
