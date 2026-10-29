package com.example.paypulse.domain.auth

enum class LoginError {
    INVALID_CREDENTIALS,
    ACCOUNT_INACTIVE,
    ACCOUNT_PENDING_ACTIVATION,
    SESSION_EXPIRED,
    NETWORK,
    UNKNOWN
}
