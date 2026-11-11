package com.example.paypulse.ui.auth.activation

import com.example.paypulse.domain.activation.ActivationRepository
import com.example.paypulse.domain.activation.ActivationResult
import com.example.paypulse.domain.activation.ActivationTokenStatus
import com.example.paypulse.domain.activation.TokenCheckResult

class StubActivationRepository : ActivationRepository {

    data class ActivationRequest(val token: String, val password: String, val confirmPassword: String)

    var tokenCheckResult: TokenCheckResult = TokenCheckResult.Checked(ActivationTokenStatus.VALID)
    var activationResult: ActivationResult = ActivationResult.Success

    val checkedTokens = mutableListOf<String>()
    val activationRequests = mutableListOf<ActivationRequest>()

    override suspend fun checkToken(token: String): TokenCheckResult {
        checkedTokens += token
        return tokenCheckResult
    }

    override suspend fun activate(token: String, password: String, confirmPassword: String): ActivationResult {
        activationRequests += ActivationRequest(token, password, confirmPassword)
        return activationResult
    }
}
