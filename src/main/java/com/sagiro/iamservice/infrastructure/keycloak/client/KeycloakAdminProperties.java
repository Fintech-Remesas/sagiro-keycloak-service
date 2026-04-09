package com.sagiro.iamservice.infrastructure.keycloak.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.keycloak")
public record KeycloakAdminProperties(
        String baseUrl,
        String realm,
        String adminClientId,
        String adminClientSecret,
        String backendClientId
) {
}
