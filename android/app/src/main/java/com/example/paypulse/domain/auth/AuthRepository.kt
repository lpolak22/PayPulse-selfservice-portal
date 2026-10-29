package com.example.paypulse.domain.auth

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionState: StateFlow<SessionState>
    suspend fun restoreSession()
    suspend fun login(email: String, password: String): LoginResult
}
