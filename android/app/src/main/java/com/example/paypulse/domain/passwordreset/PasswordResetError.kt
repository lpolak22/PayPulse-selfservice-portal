package com.example.paypulse.domain.passwordreset

enum class PasswordResetError {
    TOKEN_EXPIRED,
    TOKEN_USED,
    TOKEN_INVALID,
    INVALID_EMAIL,
    PASSWORD_POLICY,
    PASSWORD_MISMATCH,
    RATE_LIMITED,
    NETWORK,
    UNKNOWN
}
