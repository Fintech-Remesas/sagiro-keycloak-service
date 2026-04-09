package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.UpdateUserProfileCommand;
import com.sagiro.iamservice.application.dto.UserView;
import com.sagiro.iamservice.application.exception.ResourceNotFoundException;
import com.sagiro.iamservice.application.mapper.UserViewMapper;
import com.sagiro.iamservice.application.port.input.UpdateUserProfileUseCase;
import com.sagiro.iamservice.application.port.output.CurrentUserProviderPort;
import com.sagiro.iamservice.application.port.output.UserProfileRepositoryPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.domain.model.UserProfile;
import com.sagiro.iamservice.domain.valueobject.AuthenticatedUser;

import java.time.Clock;
import java.time.Instant;

public class UpdateCurrentUserProfileService implements UpdateUserProfileUseCase {

    private final CurrentUserProviderPort currentUserProviderPort;
    private final UserRepositoryPort userRepositoryPort;
    private final UserProfileRepositoryPort userProfileRepositoryPort;
    private final Clock clock;

    public UpdateCurrentUserProfileService(
            CurrentUserProviderPort currentUserProviderPort,
            UserRepositoryPort userRepositoryPort,
            UserProfileRepositoryPort userProfileRepositoryPort,
            Clock clock
    ) {
        this.currentUserProviderPort = currentUserProviderPort;
        this.userRepositoryPort = userRepositoryPort;
        this.userProfileRepositoryPort = userProfileRepositoryPort;
        this.clock = clock;
    }

    @Override
    public UserView updateCurrentUserProfile(UpdateUserProfileCommand command) {
        AuthenticatedUser authenticatedUser = currentUserProviderPort.getCurrentUser();
        User user = userRepositoryPort.findByKeycloakUserId(authenticatedUser.subject())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user is not provisioned in IAM"));
        UserProfile profile = userProfileRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User profile was not found"));

        Instant now = Instant.now(clock);
        user.updatePersonalDetails(command.phone(), command.firstName(), command.lastName(), now);
        profile.update(
                command.country(),
                command.preferredLanguage(),
                command.blockchainVisibilityEnabled(),
                command.darkModeEnabled()
        );

        User savedUser = userRepositoryPort.save(user);
        UserProfile savedProfile = userProfileRepositoryPort.save(profile);
        return UserViewMapper.toView(savedUser, savedProfile);
    }
}
