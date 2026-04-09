package com.sagiro.iamservice.domain.valueobject;

import java.util.Set;

public record AuthenticatedUser(
        String subject,
        String preferredUsername,
        String email,
        Set<String> roles
) {
}
