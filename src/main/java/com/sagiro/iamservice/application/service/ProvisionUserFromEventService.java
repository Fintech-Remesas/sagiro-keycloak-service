package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.ProvisionUserCommand;
import com.sagiro.iamservice.application.port.input.ProvisionUserFromEventUseCase;
import com.sagiro.iamservice.application.port.output.UserProfileRepositoryPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.enums.AccountStatus;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.domain.model.UserProfile;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class ProvisionUserFromEventService implements ProvisionUserFromEventUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserProfileRepositoryPort userProfileRepositoryPort;
    private final Clock clock;

    public ProvisionUserFromEventService(
            UserRepositoryPort userRepositoryPort,
            UserProfileRepositoryPort userProfileRepositoryPort,
            Clock clock
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.userProfileRepositoryPort = userProfileRepositoryPort;
        this.clock = clock;
    }

    @Override
    public void provision(ProvisionUserCommand command) {
        Instant now = Instant.now(clock);
        Optional<User> existingUser = findExistingUser(command);

        User user = existingUser.orElseGet(() -> User.registerNew(
                UUID.randomUUID(),
                command.keycloakUserId(),
                command.email(),
                command.username(),
                command.phone(),
                command.firstName(),
                command.lastName(),
                now
        ));

        user.syncIdentity(
                command.keycloakUserId(),
                command.email(),
                command.username(),
                command.phone(),
                command.firstName(),
                command.lastName(),
                now
        );
        user.markEnabled(command.enabled(), now);
        if (command.accountStatus() == AccountStatus.ACTIVE) {
            user.markActive(now);
        } else if (command.accountStatus() == AccountStatus.SUSPENDED) {
            user.markSuspended(now);
        } else if (command.accountStatus() == AccountStatus.BLOCKED) {
            user.markBlocked(now);
        }

        User savedUser = userRepositoryPort.save(user);
        UserProfile profile = userProfileRepositoryPort.findByUserId(savedUser.getId())
                .orElseGet(() -> UserProfile.createDefault(
                        UUID.randomUUID(),
                        savedUser.getId(),
                        command.country(),
                        command.preferredLanguage()
                ));
        profile.update(command.country(), command.preferredLanguage(), false, false);
        userProfileRepositoryPort.save(profile);
    }

    private Optional<User> findExistingUser(ProvisionUserCommand command) {
        if (command.keycloakUserId() != null && !command.keycloakUserId().isBlank()) {
            Optional<User> byKeycloakUserId = userRepositoryPort.findByKeycloakUserId(command.keycloakUserId());
            if (byKeycloakUserId.isPresent()) {
                return byKeycloakUserId;
            }
        }
        Optional<User> byEmail = userRepositoryPort.findByEmail(command.email());
        if (byEmail.isPresent()) {
            return byEmail;
        }
        return userRepositoryPort.findByUsername(command.username());
    }
}
