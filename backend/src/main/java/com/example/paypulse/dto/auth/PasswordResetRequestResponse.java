package com.example.paypulse.dto.auth;

public record PasswordResetRequestResponse(String message) {

    public static final String SENT_IF_EXISTS = "If an account exists for this email, we've sent a reset link.";

    public static PasswordResetRequestResponse sentIfExists() {
        return new PasswordResetRequestResponse(SENT_IF_EXISTS);
    }
}
