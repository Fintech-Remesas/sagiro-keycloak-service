package com.sagiro.iamservice.application.service;

import com.sagiro.iamservice.application.dto.KeycloakUserDraft;
import com.sagiro.iamservice.application.dto.RegisterUserCommand;
import com.sagiro.iamservice.application.dto.UserView;
import com.sagiro.iamservice.application.exception.ConflictException;
import com.sagiro.iamservice.application.mapper.UserViewMapper;
import com.sagiro.iamservice.application.port.input.RegisterUserUseCase;
import com.sagiro.iamservice.application.port.output.KeycloakAdminPort;
import com.sagiro.iamservice.application.port.output.UserProfileRepositoryPort;
import com.sagiro.iamservice.application.port.output.UserRepositoryPort;
import com.sagiro.iamservice.domain.model.User;
import com.sagiro.iamservice.domain.model.UserProfile;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class RegisterUserService implements RegisterUserUseCase {

    private static final Set<String> DEFAULT_REALM_ROLES = Set.of("ROLE_CUSTOMER");

    private final UserRepositoryPort userRepositoryPort;
    private final UserProfileRepositoryPort userProfileRepositoryPort;
    private final KeycloakAdminPort keycloakAdminPort;
    private final Clock clock;

    public RegisterUserService(
            UserRepositoryPort userRepositoryPort,
            UserProfileRepositoryPort userProfileRepositoryPort,
            KeycloakAdminPort keycloakAdminPort,
            Clock clock
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.userProfileRepositoryPort = userProfileRepositoryPort;
        this.keycloakAdminPort = keycloakAdminPort;
        this.clock = clock;
    }

    @Override
    public UserView register(RegisterUserCommand command) {
        if (userRepositoryPort.existsByEmail(command.email())) {
            throw new ConflictException("Este correo ya está registrado");
        }
        if (userRepositoryPort.existsByUsername(command.username())) {
            throw new ConflictException("Este nombre de usuario ya está en uso");
        }
        if (keycloakAdminPort.findUser(command.email(), command.username()).isPresent()) {
            throw new ConflictException("La identidad en Keycloak para este usuario o correo ya existe");
        }

        String keycloakUserId = keycloakAdminPort.createUser(
                new KeycloakUserDraft(
                        command.username(),
                        command.email(),
                        command.firstName(),
                        command.lastName(),
                        true,
                        command.initialPassword(),
                        DEFAULT_REALM_ROLES
                )
        );
        keycloakAdminPort.assignRealmRoles(keycloakUserId, DEFAULT_REALM_ROLES);

        Instant now = Instant.now(clock);
        User user = User.registerNew(
                UUID.randomUUID(),
                keycloakUserId,
                command.email(),
                command.username(),
                command.phone(),
                command.firstName(),
                command.lastName(),
                now
        );
        User savedUser = userRepositoryPort.save(user);

        UserProfile profile = UserProfile.createDefault(
                UUID.randomUUID(),
                savedUser.getId(),
                command.country(),
                command.preferredLanguage()
        );
        UserProfile savedProfile = userProfileRepositoryPort.save(profile);

        return UserViewMapper.toView(savedUser, savedProfile);
    }
}
