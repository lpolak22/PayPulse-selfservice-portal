package com.example.paypulse.domain.activation

enum class ActivationError {
    TOKEN_EXPIRED,
    TOKEN_USED,
    TOKEN_INVALID,
    PASSWORD_POLICY,
    PASSWORD_MISMATCH,
    NETWORK,
    UNKNOWN
}
