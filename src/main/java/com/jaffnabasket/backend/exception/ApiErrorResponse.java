package com.jaffnabasket.backend.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        String code,
        String message,
        List<FieldErrorItem> fieldErrors,
        String correlationId,
        Instant timestamp
) {

    public record FieldErrorItem(String field, String message) {
    }

    public static ApiErrorResponse of(String code, String message, List<FieldErrorItem> fieldErrors, String correlationId) {
        return new ApiErrorResponse(code, message, fieldErrors, correlationId, Instant.now());
    }
}
