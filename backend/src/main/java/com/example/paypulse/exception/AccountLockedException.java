package com.example.paypulse.exception;

public class AccountLockedException extends AuthException {

    private final long retryAfterMinutes;

    public AccountLockedException(long retryAfterMinutes) {
        super(ErrorCode.ACCOUNT_LOCKED);
        this.retryAfterMinutes = retryAfterMinutes;
    }

    public long getRetryAfterMinutes() {
        return retryAfterMinutes;
    }
}
