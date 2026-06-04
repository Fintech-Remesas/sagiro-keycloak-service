package com.sagiro.iamservice.infrastructure.web.controller;

import com.sagiro.iamservice.infrastructure.keycloak.client.KeycloakAdminProperties;
import com.sagiro.iamservice.infrastructure.shared.ApiResponse;
import com.sagiro.iamservice.infrastructure.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication endpoints to request JWT tokens from Keycloak")
public class AuthController {

    private final RestClient restClient;
    private final KeycloakAdminProperties properties;

    public AuthController(RestClient.Builder restClientBuilder, KeycloakAdminProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Request a user bearer token from Keycloak",
            description = "Proxies the login request to Keycloak's openid-connect/token endpoint using the public client."
    )
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "password");
            formData.add("client_id", "remittance-frontend");
            formData.add("username", request.username());
            formData.add("password", request.password());

            Map<?, ?> tokenResponse = restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", properties.realm())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(Map.class);

            return ResponseEntity.ok(ApiResponse.success("Token obtained successfully", tokenResponse));
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(ApiErrorResponse.of("AUTHENTICATION_FAILED", "Authentication failed: " + ex.getResponseBodyAsString(), List.of()));
        }
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}
}
