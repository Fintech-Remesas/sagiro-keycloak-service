package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.UserView;
import com.sagiro.iamservice.application.exception.ResourceNotFoundException;
import com.sagiro.iamservice.application.mapper.UserViewMapper;
import com.sagiro.iamservice.application.port.input.GetCurrentUserUseCase;
import com.sagiro.iamservice.application.port.output.CurrentUserProviderPort;
import com.sagiro.iamservice.application.port.output.UserProfileRepositoryPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.domain.model.UserProfile;
import com.sagiro.iamservice.domain.valueobject.AuthenticatedUser;

public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final CurrentUserProviderPort currentUserProviderPort;
    private final UserRepositoryPort userRepositoryPort;
    private final UserProfileRepositoryPort userProfileRepositoryPort;

    public GetCurrentUserService(
            CurrentUserProviderPort currentUserProviderPort,
            UserRepositoryPort userRepositoryPort,
            UserProfileRepositoryPort userProfileRepositoryPort
    ) {
        this.currentUserProviderPort = currentUserProviderPort;
        this.userRepositoryPort = userRepositoryPort;
        this.userProfileRepositoryPort = userProfileRepositoryPort;
    }

    @Override
    public UserView getCurrentUser() {
        AuthenticatedUser authenticatedUser = currentUserProviderPort.getCurrentUser();
        User user = userRepositoryPort.findByKeycloakUserId(authenticatedUser.subject())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user is not provisioned in IAM"));
        UserProfile profile = userProfileRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User profile was not found"));
        return UserViewMapper.toView(user, profile);
    }
}
