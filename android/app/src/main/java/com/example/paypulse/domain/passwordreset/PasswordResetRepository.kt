package com.example.paypulse.domain.passwordreset

interface PasswordResetRepository {
    suspend fun requestReset(email: String): PasswordResetResult
    suspend fun checkToken(token: String): ResetTokenCheckResult
    suspend fun resetPassword(token: String, password: String, confirmPassword: String): PasswordResetResult
}
