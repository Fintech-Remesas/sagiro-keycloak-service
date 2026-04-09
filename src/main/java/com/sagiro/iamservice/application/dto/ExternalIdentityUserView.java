package com.sagiro.iamservice.application.dto;

public record ExternalIdentityUserView(
        String id,
        String username,
        String email,
        boolean enabled
) {
}
