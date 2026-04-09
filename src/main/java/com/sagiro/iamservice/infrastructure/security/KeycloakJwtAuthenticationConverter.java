package com.sagiro.iamservice.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtApplicationPrincipalAdapter jwtApplicationPrincipalAdapter;

    public KeycloakJwtAuthenticationConverter(JwtApplicationPrincipalAdapter jwtApplicationPrincipalAdapter) {
        this.jwtApplicationPrincipalAdapter = jwtApplicationPrincipalAdapter;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        ApplicationPrincipal principal = jwtApplicationPrincipalAdapter.fromJwt(jwt);
        Collection<GrantedAuthority> authorities = principal.roles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
        String principalName = principal.preferredUsername() != null ? principal.preferredUsername() : principal.subject();
        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }
}
