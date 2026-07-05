package com.sagiro.iamservice.application.dto;

import java.util.UUID;

public record RemittanceContextView(
        UUID userId,
        String country,
        String firstName,
        String lastName
) {
}
