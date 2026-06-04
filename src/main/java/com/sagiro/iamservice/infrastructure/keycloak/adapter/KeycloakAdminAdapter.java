package com.sagiro.iamservice.infrastructure.keycloak.adapter;

import com.sagiro.iamservice.application.dto.ExternalIdentityUserView;
import com.sagiro.iamservice.application.dto.KeycloakUserDraft;
import com.sagiro.iamservice.application.port.output.KeycloakAdminPort;
import com.sagiro.iamservice.infrastructure.keycloak.client.KeycloakAdminClient;
import com.sagiro.iamservice.infrastructure.keycloak.dto.KeycloakCreateUserRequest;
import com.sagiro.iamservice.infrastructure.keycloak.dto.KeycloakCredentialRepresentation;
import com.sagiro.iamservice.infrastructure.keycloak.dto.KeycloakRoleRepresentation;
import com.sagiro.iamservice.infrastructure.keycloak.dto.KeycloakUserRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class KeycloakAdminAdapter implements KeycloakAdminPort {

    private final KeycloakAdminClient keycloakAdminClient;

    public KeycloakAdminAdapter(KeycloakAdminClient keycloakAdminClient) {
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @Override
    public String createUser(KeycloakUserDraft userDraft) {
        List<KeycloakCredentialRepresentation> credentials = userDraft.temporaryPassword() == null || userDraft.temporaryPassword().isBlank()
                ? List.of()
                : List.of(new KeycloakCredentialRepresentation("password", userDraft.temporaryPassword(), false));
        List<String> requiredActions = credentials.isEmpty() ? List.of("UPDATE_PASSWORD") : List.of();

        return keycloakAdminClient.createUser(new KeycloakCreateUserRequest(
                userDraft.username(),
                userDraft.email(),
                userDraft.firstName(),
                userDraft.lastName(),
                userDraft.enabled(),
                requiredActions,
                credentials
        ));
    }

    @Override
    public Optional<ExternalIdentityUserView> findUser(String email, String username) {
        return keycloakAdminClient.searchUsers(email, username).stream()
                .findFirst()
                .map(this::toView);
    }

    @Override
    public void assignRealmRoles(String keycloakUserId, Set<String> roleNames) {
        List<KeycloakRoleRepresentation> roles = roleNames.stream()
                .map(keycloakAdminClient::getRealmRole)
                .toList();
        if (!roles.isEmpty()) {
            keycloakAdminClient.assignRealmRoles(keycloakUserId, roles);
        }
    }

    @Override
    public void disableUser(String keycloakUserId) {
        keycloakAdminClient.disableUser(keycloakUserId);
    }

    private ExternalIdentityUserView toView(KeycloakUserRepresentation representation) {
        return new ExternalIdentityUserView(
                representation.id(),
                representation.username(),
                representation.email(),
                representation.enabled()
        );
    }
}
