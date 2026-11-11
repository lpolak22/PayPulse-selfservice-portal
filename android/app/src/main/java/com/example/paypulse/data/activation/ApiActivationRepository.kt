package com.example.paypulse.data.activation

import com.example.paypulse.data.remote.ActivationApi
import com.example.paypulse.data.remote.ErrorResponseParser
import com.example.paypulse.data.remote.dto.ActivateAccountRequestDto
import com.example.paypulse.data.remote.mapper.activationErrorFrom
import com.example.paypulse.data.remote.mapper.toTokenStatus
import com.example.paypulse.domain.activation.ActivationError
import com.example.paypulse.domain.activation.ActivationRepository
import com.example.paypulse.domain.activation.ActivationResult
import com.example.paypulse.domain.activation.TokenCheckResult
import retrofit2.HttpException
import java.io.IOException

class ApiActivationRepository(
    private val activationApi: ActivationApi,
    private val errorResponseParser: ErrorResponseParser
) : ActivationRepository {

    override suspend fun checkToken(token: String): TokenCheckResult = try {
        activationApi.checkToken(token).toTokenStatus()
            ?.let { TokenCheckResult.Checked(it) }
            ?: TokenCheckResult.Failure(ActivationError.UNKNOWN)
    } catch (exception: Exception) {
        TokenCheckResult.Failure(activationErrorOf(exception) ?: throw exception)
    }

    override suspend fun activate(token: String, password: String, confirmPassword: String): ActivationResult = try {
        activationApi.activate(ActivateAccountRequestDto(token, password, confirmPassword))
        ActivationResult.Success
    } catch (exception: Exception) {
        ActivationResult.Failure(activationErrorOf(exception) ?: throw exception)
    }

    private fun activationErrorOf(exception: Exception): ActivationError? = when (exception) {
        is HttpException -> errorFromResponse(exception)
        is IOException -> ActivationError.NETWORK
        is IllegalArgumentException -> ActivationError.UNKNOWN
        else -> null
    }

    private fun errorFromResponse(exception: HttpException): ActivationError {
        val apiError = errorResponseParser.apiError(exception)
        return activationErrorFrom(apiError?.code, apiError?.fieldNames.orEmpty())
    }
}
