package com.example.paypulse.exception;

import org.springframework.http.HttpStatus;

/**
 * Machine-readable error codes returned in {@link com.example.paypulse.dto.common.ErrorResponse}.
 * The frontend can switch on these values to show the right message.
 */
public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "One or more fields are invalid."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "The request is malformed or has invalid parameters."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication is required."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "You do not have permission to perform this action."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource was not found."),
    CONFLICT(HttpStatus.CONFLICT, "The request conflicts with the current state of the resource."),
    USER_EMAIL_EXISTS(HttpStatus.CONFLICT, "A user with this email already exists."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}