package com.sagiro.iamservice.infrastructure.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class JwtApplicationPrincipalAdapter {

    public ApplicationPrincipal fromJwt(Jwt jwt) {
        return new ApplicationPrincipal(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                extractRoles(jwt)
        );
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRoles(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            Object rawRoles = realmAccess.get("roles");
            if (rawRoles instanceof List<?> roleList) {
                roleList.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(this::normalizeRole)
                        .forEach(roles::add);
            }
        }

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            resourceAccess.values().stream()
                    .filter(Map.class::isInstance)
                    .map(Map.class::cast)
                    .forEach(clientData -> {
                        Object rawRoles = clientData.get("roles");
                        if (rawRoles instanceof List<?> roleList) {
                            roleList.stream()
                                    .filter(String.class::isInstance)
                                    .map(String.class::cast)
                                    .map(this::normalizeRole)
                                    .forEach(roles::add);
                        }
                    });
        }
        return roles;
    }

    private String normalizeRole(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
}
