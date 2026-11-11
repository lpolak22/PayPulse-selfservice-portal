package com.example.paypulse.domain.activation

sealed interface TokenCheckResult {
    data class Checked(val status: ActivationTokenStatus) : TokenCheckResult
    data class Failure(val error: ActivationError) : TokenCheckResult
}
