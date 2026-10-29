package com.example.paypulse.domain.activation

sealed interface ActivationResult {
    data object Success : ActivationResult
    data class Failure(val error: ActivationError) : ActivationResult
}
