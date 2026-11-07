package com.example.paypulse.integration.sevenpay;

public class SevenPayException extends RuntimeException {

    private final int status;

    public SevenPayException(int status, String message) {
        super(message);
        this.status = status;
    }

    public SevenPayException(String message, Throwable cause) {
        super(message, cause);
        this.status = 0;
    }

    public int getStatus() {
        return status;
    }

    public boolean isAccessDenied() {
        return status == 401 || status == 403;
    }

    public boolean isRetryable() {
        return status == 0 || status == 500 || status == 503;
    }
}
