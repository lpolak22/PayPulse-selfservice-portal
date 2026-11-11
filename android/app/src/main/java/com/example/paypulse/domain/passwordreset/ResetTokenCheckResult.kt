package com.example.paypulse.domain.passwordreset

sealed interface ResetTokenCheckResult {
    data class Checked(val status: ResetTokenStatus) : ResetTokenCheckResult
    data class Failure(val error: PasswordResetError) : ResetTokenCheckResult
}
