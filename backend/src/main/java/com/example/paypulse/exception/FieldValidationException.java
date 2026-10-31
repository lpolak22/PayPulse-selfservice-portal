package com.example.paypulse.exception;

import com.example.paypulse.dto.common.ErrorResponse;

import java.util.List;

public class FieldValidationException extends ApiException {

    private final List<ErrorResponse.FieldError> fieldErrors;

    public FieldValidationException(List<ErrorResponse.FieldError> fieldErrors) {
        super(ErrorCode.VALIDATION_ERROR);
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public List<ErrorResponse.FieldError> getFieldErrors() {
        return fieldErrors;
    }
}
