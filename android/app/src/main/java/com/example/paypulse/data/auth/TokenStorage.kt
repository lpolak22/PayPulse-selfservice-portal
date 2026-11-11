package com.example.paypulse.data.auth

interface TokenStorage {
    suspend fun read(): AuthTokens?
    suspend fun save(tokens: AuthTokens)
    suspend fun clear()
}
