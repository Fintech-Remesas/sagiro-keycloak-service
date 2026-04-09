package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.AccessContextView;
import com.sagiro.iamservice.application.exception.ResourceNotFoundException;
import com.sagiro.iamservice.application.port.input.GetAccessContextUseCase;
import com.sagiro.iamservice.application.port.output.CurrentUserProviderPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.domain.valueobject.AuthenticatedUser;

public class GetAccessContextService implements GetAccessContextUseCase {

    private final CurrentUserProviderPort currentUserProviderPort;
    private final UserRepositoryPort userRepositoryPort;

    public GetAccessContextService(
            CurrentUserProviderPort currentUserProviderPort,
            UserRepositoryPort userRepositoryPort
    ) {
        this.currentUserProviderPort = currentUserProviderPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public AccessContextView getCurrentAccessContext() {
        AuthenticatedUser authenticatedUser = currentUserProviderPort.getCurrentUser();
        User user = userRepositoryPort.findByKeycloakUserId(authenticatedUser.subject())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user is not provisioned in IAM"));

        return new AccessContextView(
                user.getId(),
                user.getKeycloakUserId(),
                authenticatedUser.subject(),
                authenticatedUser.preferredUsername(),
                authenticatedUser.email(),
                authenticatedUser.roles(),
                user.getAccountStatus(),
                user.getVerificationStatus(),
                user.getVerificationLevel(),
                user.isCanOperate(),
                user.isEnabled()
        );
    }
}
