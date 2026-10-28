package com.example.paypulse.domain.auth

interface AuthRepository {
    suspend fun login(email: String, password: String): LoginResult
}
