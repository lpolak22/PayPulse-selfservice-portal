package com.example.paypulse.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record ErrorResponse(String code, String message, List<FieldError> fieldErrors,
                            @JsonInclude(JsonInclude.Include.NON_NULL) Integer remainingAttempts,
                            @JsonInclude(JsonInclude.Include.NON_NULL) Long retryAfterMinutes) {

    public ErrorResponse {
        fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    public ErrorResponse(String code, String message, List<FieldError> fieldErrors) {
        this(code, message, fieldErrors, null, null);
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of());
    }

    public static ErrorResponse withRemainingAttempts(String code, String message, int remainingAttempts) {
        return new ErrorResponse(code, message, List.of(), remainingAttempts, null);
    }

    public static ErrorResponse withRetryAfterMinutes(String code, String message, long retryAfterMinutes) {
        return new ErrorResponse(code, message, List.of(), null, retryAfterMinutes);
    }

    public record FieldError(String field, String message) {
    }
}
