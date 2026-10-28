package com.example.paypulse.domain.auth

sealed interface LoginResult {
    data object Success : LoginResult
    data class Failure(val error: LoginError) : LoginResult
}
