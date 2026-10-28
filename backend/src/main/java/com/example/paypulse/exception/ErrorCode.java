package com.example.paypulse.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "One or more fields are invalid."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "The request is malformed or has invalid parameters."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication is required."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "You do not have permission to perform this action."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource was not found."),
    CONFLICT(HttpStatus.CONFLICT, "The request conflicts with the current state of the resource."),
    USER_EMAIL_EXISTS(HttpStatus.CONFLICT, "A user with this email already exists."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password."),
    ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "Your account is deactivated. Please contact your admin."),
    ACCOUNT_PENDING_ACTIVATION(HttpStatus.FORBIDDEN, "Your account is not activated yet. Please use the link from your activation email."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "The session has expired. Please log in again."),
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