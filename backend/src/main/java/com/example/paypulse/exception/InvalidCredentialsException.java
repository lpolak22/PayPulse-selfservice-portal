package com.example.paypulse.exception;

public class InvalidCredentialsException extends AuthException {

    private final int remainingAttempts;

    public InvalidCredentialsException(int remainingAttempts) {
        super(ErrorCode.INVALID_CREDENTIALS);
        this.remainingAttempts = remainingAttempts;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }
}
