package com.example.paypulse.data.auth

import com.example.paypulse.domain.auth.AuthRepository
import com.example.paypulse.domain.auth.LoginError
import com.example.paypulse.domain.auth.LoginResult
import kotlinx.coroutines.delay

private const val RESPONSE_DELAY_MS = 1_000L

private val resultsByEmail: Map<String, LoginResult> = mapOf(
    "aktivan@primjer.hr" to LoginResult.Success,
    "neaktivan@primjer.hr" to LoginResult.Failure(LoginError.ACCOUNT_INACTIVE),
    "neaktiviran@primjer.hr" to LoginResult.Failure(LoginError.ACCOUNT_PENDING_ACTIVATION),
    "mreza@primjer.hr" to LoginResult.Failure(LoginError.NETWORK)
)

class FakeAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String): LoginResult {
        delay(RESPONSE_DELAY_MS)
        if (email.isBlank() || password.isBlank()) return LoginResult.Failure(LoginError.UNKNOWN)
        return resultsByEmail[email.trim()] ?: LoginResult.Failure(LoginError.INVALID_CREDENTIALS)
    }
}
