package com.example.paypulse.domain.activation

interface ActivationRepository {
    suspend fun checkToken(token: String): TokenCheckResult
    suspend fun activate(token: String, password: String, confirmPassword: String): ActivationResult
}
