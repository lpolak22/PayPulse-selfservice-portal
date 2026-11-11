package com.example.paypulse.data.activation

import com.example.paypulse.data.remote.ActivationApiContract
import com.example.paypulse.data.remote.mapper.activationErrorFrom
import com.example.paypulse.domain.activation.ActivationError
import com.example.paypulse.domain.activation.ActivationRepository
import com.example.paypulse.domain.activation.ActivationResult
import com.example.paypulse.domain.activation.ActivationTokenStatus
import com.example.paypulse.domain.activation.TokenCheckResult
import com.example.paypulse.domain.auth.PasswordPolicy
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val FAKE_DELAY_MILLIS = 500L
private const val OFFLINE_TOKEN = "offline"

class FakeActivationRepository : ActivationRepository {

    private val mutex = Mutex()
    private val statusesByToken = mutableMapOf(
        "valid" to ActivationTokenStatus.VALID,
        "expired" to ActivationTokenStatus.EXPIRED,
        "used" to ActivationTokenStatus.USED
    )

    override suspend fun checkToken(token: String): TokenCheckResult {
        delay(FAKE_DELAY_MILLIS)
        if (token == OFFLINE_TOKEN) return TokenCheckResult.Failure(ActivationError.NETWORK)
        val status = mutex.withLock { statusesByToken[token] }
            ?: return TokenCheckResult.Failure(ActivationError.TOKEN_INVALID)
        return TokenCheckResult.Checked(status)
    }

    override suspend fun activate(token: String, password: String, confirmPassword: String): ActivationResult {
        delay(FAKE_DELAY_MILLIS)
        if (token == OFFLINE_TOKEN) return ActivationResult.Failure(ActivationError.NETWORK)
        return mutex.withLock { useToken(token, password, confirmPassword) }
    }

    private fun useToken(token: String, password: String, confirmPassword: String): ActivationResult {
        tokenErrorCode(token)?.let { return ActivationResult.Failure(activationErrorFrom(it)) }
        val invalidFields = invalidFields(password, confirmPassword)
        if (invalidFields.isNotEmpty()) {
            return ActivationResult.Failure(activationErrorFrom(ActivationApiContract.CODE_VALIDATION_ERROR, invalidFields))
        }
        statusesByToken[token] = ActivationTokenStatus.USED
        return ActivationResult.Success
    }

    private fun tokenErrorCode(token: String): String? = when (statusesByToken[token]) {
        ActivationTokenStatus.VALID -> null
        ActivationTokenStatus.EXPIRED -> ActivationApiContract.CODE_TOKEN_EXPIRED
        ActivationTokenStatus.USED -> ActivationApiContract.CODE_TOKEN_USED
        null -> ActivationApiContract.CODE_TOKEN_INVALID
    }

    private fun invalidFields(password: String, confirmPassword: String): List<String> = buildList {
        if (!PasswordPolicy.isSatisfiedBy(password)) add(ActivationApiContract.FIELD_PASSWORD)
        if (password != confirmPassword) add(ActivationApiContract.FIELD_CONFIRM_PASSWORD)
    }
}
