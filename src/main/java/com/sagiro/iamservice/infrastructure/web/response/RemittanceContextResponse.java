package com.sagiro.iamservice.infrastructure.web.response;

import java.util.UUID;

public record RemittanceContextResponse(
        UUID userId,
        String country,
        String firstName,
        String lastName
) {
}
