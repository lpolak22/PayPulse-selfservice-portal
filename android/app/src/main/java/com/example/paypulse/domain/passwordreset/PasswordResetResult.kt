package com.example.paypulse.domain.passwordreset

sealed interface PasswordResetResult {
    data object Success : PasswordResetResult
    data class Failure(val error: PasswordResetError) : PasswordResetResult
}
