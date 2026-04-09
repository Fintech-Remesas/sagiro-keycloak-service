package com.sagiro.iamservice.infrastructure.security;

import com.sagiro.iamservice.application.exception.UnauthorizedException;
import com.sagiro.iamservice.application.port.output.CurrentUserProviderPort;
import com.sagiro.iamservice.domain.valueobject.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentUserProviderAdapter implements CurrentUserProviderPort {

    private final JwtApplicationPrincipalAdapter jwtApplicationPrincipalAdapter;

    public SecurityContextCurrentUserProviderAdapter(JwtApplicationPrincipalAdapter jwtApplicationPrincipalAdapter) {
        this.jwtApplicationPrincipalAdapter = jwtApplicationPrincipalAdapter;
    }

    @Override
    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new UnauthorizedException("No authenticated JWT principal is available in the security context");
        }
        ApplicationPrincipal principal = jwtApplicationPrincipalAdapter.fromJwt(jwt);
        return new AuthenticatedUser(
                principal.subject(),
                principal.preferredUsername(),
                principal.email(),
                principal.roles()
        );
    }
}
