package com.example.paypulse.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record ErrorResponse(String code, String message, List<FieldError> fieldErrors,
                            @JsonInclude(JsonInclude.Include.NON_NULL) Integer remainingAttempts) {

    public ErrorResponse {
        fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    public ErrorResponse(String code, String message, List<FieldError> fieldErrors) {
        this(code, message, fieldErrors, null);
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of());
    }

    public record FieldError(String field, String message) {
    }
}
