package com.example.paypulse.exception;

public class AuthException extends ApiException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}
