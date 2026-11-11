package com.example.paypulse.data.remote

object PasswordResetApiContract {
    const val CODE_VALIDATION_ERROR = "VALIDATION_ERROR"
    const val CODE_TOKEN_EXPIRED = "PASSWORD_RESET_TOKEN_EXPIRED"
    const val CODE_TOKEN_USED = "PASSWORD_RESET_TOKEN_USED"
    const val CODE_TOKEN_INVALID = "PASSWORD_RESET_TOKEN_INVALID"
    const val CODE_RATE_LIMITED = "PASSWORD_RESET_RATE_LIMITED"
    const val FIELD_EMAIL = "email"
    const val FIELD_PASSWORD = "password"
    const val FIELD_CONFIRM_PASSWORD = "confirmPassword"
}
