package com.example.paypulse.dto.common;

import java.util.List;

/**
 * Standard error body returned by every endpoint.
 *
 * @param code        machine-readable error code (see {@code ErrorCode})
 * @param message     human-readable description
 * @param fieldErrors list of invalid fields; empty when the error is not field-specific
 */
public record ErrorResponse(String code, String message, List<FieldError> fieldErrors) {

    public ErrorResponse {
        fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of());
    }

    public record FieldError(String field, String message) {
    }
}
