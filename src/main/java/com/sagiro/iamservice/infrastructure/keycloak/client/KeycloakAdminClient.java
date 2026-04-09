package com.sagiro.iamservice.infrastructure.keycloak.client;

import com.sagiro.iamservice.application.exception.ExternalServiceException;
import com.sagiro.iamservice.infrastructure.keycloak.dto.KeycloakCreateUserRequest;
import com.sagiro.iamservice.infrastructure.keycloak.dto.KeycloakRoleRepresentation;
import com.sagiro.iamservice.infrastructure.keycloak.dto.KeycloakTokenResponse;
import com.sagiro.iamservice.infrastructure.keycloak.dto.KeycloakUserRepresentation;
import com.sagiro.iamservice.infrastructure.keycloak.dto.KeycloakUserUpdateRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Component
public class KeycloakAdminClient {

    private final RestClient restClient;
    private final KeycloakAdminProperties properties;

    public KeycloakAdminClient(RestClient.Builder restClientBuilder, KeycloakAdminProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
    }

    public String createUser(KeycloakCreateUserRequest request) {
        String token = obtainTechnicalToken();
        try {
            var response = restClient.post()
                    .uri("/admin/realms/{realm}/users", properties.realm())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            URI location = response.getHeaders().getLocation();
            if (location == null) {
                throw new ExternalServiceException("Keycloak did not return the created user location");
            }
            String path = location.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (RestClientResponseException exception) {
            throw new ExternalServiceException("Failed to create user in Keycloak: " + exception.getResponseBodyAsString(), exception);
        }
    }

    public List<KeycloakUserRepresentation> searchUsers(String email, String username) {
        String token = obtainTechnicalToken();
        try {
            KeycloakUserRepresentation[] byEmail = new KeycloakUserRepresentation[0];
            if (email != null && !email.isBlank()) {
                byEmail = restClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/admin/realms/{realm}/users")
                                .queryParam("email", email)
                                .queryParam("exact", true)
                                .build(properties.realm()))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .retrieve()
                        .body(KeycloakUserRepresentation[].class);
            }
            if (byEmail != null && byEmail.length > 0) {
                return Arrays.stream(byEmail).toList();
            }
            if (username == null || username.isBlank()) {
                return List.of();
            }
            KeycloakUserRepresentation[] byUsername = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/admin/realms/{realm}/users")
                            .queryParam("username", username)
                            .queryParam("exact", true)
                            .build(properties.realm()))
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .retrieve()
                    .body(KeycloakUserRepresentation[].class);
            return byUsername == null ? List.of() : Arrays.stream(byUsername).toList();
        } catch (RestClientResponseException exception) {
            throw new ExternalServiceException("Failed to search users in Keycloak: " + exception.getResponseBodyAsString(), exception);
        }
    }

    public KeycloakRoleRepresentation getRealmRole(String roleName) {
        String token = obtainTechnicalToken();
        try {
            return restClient.get()
                    .uri("/admin/realms/{realm}/roles/{roleName}", properties.realm(), roleName)
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .retrieve()
                    .body(KeycloakRoleRepresentation.class);
        } catch (RestClientResponseException exception) {
            throw new ExternalServiceException("Failed to read realm role from Keycloak: " + exception.getResponseBodyAsString(), exception);
        }
    }

    public void assignRealmRoles(String keycloakUserId, List<KeycloakRoleRepresentation> roles) {
        String token = obtainTechnicalToken();
        try {
            restClient.post()
                    .uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", properties.realm(), keycloakUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .body(roles)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new ExternalServiceException("Failed to assign realm roles in Keycloak: " + exception.getResponseBodyAsString(), exception);
        }
    }

    public void disableUser(String keycloakUserId) {
        String token = obtainTechnicalToken();
        try {
            restClient.put()
                    .uri("/admin/realms/{realm}/users/{userId}", properties.realm(), keycloakUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .body(new KeycloakUserUpdateRequest(false))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new ExternalServiceException("Failed to disable user in Keycloak: " + exception.getResponseBodyAsString(), exception);
        }
    }

    private String obtainTechnicalToken() {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "client_credentials");
            formData.add("client_id", properties.adminClientId());
            formData.add("client_secret", properties.adminClientSecret());

            KeycloakTokenResponse response = restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", properties.realm())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(KeycloakTokenResponse.class);

            if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
                throw new ExternalServiceException("Keycloak admin token response did not include an access token");
            }
            return response.accessToken();
        } catch (RestClientResponseException exception) {
            throw new ExternalServiceException("Failed to obtain technical token from Keycloak: " + exception.getResponseBodyAsString(), exception);
        }
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
