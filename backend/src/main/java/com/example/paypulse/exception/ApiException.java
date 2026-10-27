package com.example.paypulse.exception;

/**
 * Base exception for expected business errors. The global handler turns it into
 * an {@link com.example.paypulse.dto.common.ErrorResponse} with the status of its {@link ErrorCode}.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage());
    }

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
