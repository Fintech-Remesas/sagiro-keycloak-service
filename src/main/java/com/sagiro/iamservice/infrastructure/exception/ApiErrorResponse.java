package com.sagiro.iamservice.infrastructure.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        boolean success,
        String code,
        String message,
        List<String> details
) {
    public static ApiErrorResponse of(String code, String message, List<String> details) {
        return new ApiErrorResponse(Instant.now(), false, code, message, details);
    }
}
