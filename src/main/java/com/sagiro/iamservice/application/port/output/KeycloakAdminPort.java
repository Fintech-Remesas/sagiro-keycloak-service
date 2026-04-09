package com.sagiro.iamservice.application.port.output;

import com.sagiro.iamservice.application.dto.ExternalIdentityUserView;
import com.sagiro.iamservice.application.dto.KeycloakUserDraft;

import java.util.Optional;
import java.util.Set;

public interface KeycloakAdminPort {

    String createUser(KeycloakUserDraft userDraft);

    Optional<ExternalIdentityUserView> findUser(String email, String username);

    void assignRealmRoles(String keycloakUserId, Set<String> roleNames);

    void disableUser(String keycloakUserId);
}
