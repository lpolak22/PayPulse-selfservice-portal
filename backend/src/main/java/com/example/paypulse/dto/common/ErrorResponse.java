package com.example.paypulse.dto.common;

import java.util.List;

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
