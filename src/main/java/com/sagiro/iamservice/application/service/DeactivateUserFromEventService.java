package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.DeactivateUserCommand;
import com.sagiro.iamservice.application.port.input.DeactivateUserFromEventUseCase;
import com.sagiro.iamservice.application.port.output.KeycloakAdminPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;

import java.time.Clock;
import java.time.Instant;

public class DeactivateUserFromEventService implements DeactivateUserFromEventUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final KeycloakAdminPort keycloakAdminPort;
    private final Clock clock;

    public DeactivateUserFromEventService(
            UserRepositoryPort userRepositoryPort,
            KeycloakAdminPort keycloakAdminPort,
            Clock clock
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.keycloakAdminPort = keycloakAdminPort;
        this.clock = clock;
    }

    @Override
    public void deactivate(DeactivateUserCommand command) {
        if (command.keycloakUserId() != null && !command.keycloakUserId().isBlank()) {
            userRepositoryPort.findByKeycloakUserId(command.keycloakUserId())
                    .ifPresent(user -> blockUser(user, Instant.now(clock)));
        } else if (command.email() != null && !command.email().isBlank()) {
            userRepositoryPort.findByEmail(command.email())
                    .ifPresent(user -> blockUser(user, Instant.now(clock)));
        } else if (command.username() != null && !command.username().isBlank()) {
            userRepositoryPort.findByUsername(command.username())
                    .ifPresent(user -> blockUser(user, Instant.now(clock)));
        }

        if (command.keycloakUserId() != null && !command.keycloakUserId().isBlank()) {
            keycloakAdminPort.disableUser(command.keycloakUserId());
        }
    }

    private void blockUser(User user, Instant now) {
        user.markBlocked(now);
        userRepositoryPort.save(user);
    }
}
