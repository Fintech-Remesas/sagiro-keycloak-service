package com.sagiro.iamservice.infrastructure.web.response;

import java.util.Set;

public record TestEndpointResponse(
        String message,
        String subject,
        String username,
        Set<String> roles
) {
}
