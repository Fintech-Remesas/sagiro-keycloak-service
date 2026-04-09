package com.sagiro.iamservice.infrastructure.security;

import java.util.Set;

public record ApplicationPrincipal(
        String subject,
        String preferredUsername,
        String email,
        Set<String> roles
) {
}
