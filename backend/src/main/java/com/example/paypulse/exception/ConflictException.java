package com.example.paypulse.exception;

/** Thrown when a request conflicts with existing data, e.g. a duplicate email (HTTP 409). */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
}
