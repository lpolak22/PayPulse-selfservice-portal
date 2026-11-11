package com.example.paypulse.data.passwordreset

import com.example.paypulse.data.remote.ErrorResponseParser
import com.example.paypulse.data.remote.PasswordResetApi
import com.example.paypulse.data.remote.dto.PasswordResetConfirmRequestDto
import com.example.paypulse.data.remote.dto.PasswordResetRequestDto
import com.example.paypulse.data.remote.mapper.passwordResetErrorFrom
import com.example.paypulse.data.remote.mapper.toResetTokenStatus
import com.example.paypulse.domain.passwordreset.PasswordResetError
import com.example.paypulse.domain.passwordreset.PasswordResetRepository
import com.example.paypulse.domain.passwordreset.PasswordResetResult
import com.example.paypulse.domain.passwordreset.ResetTokenCheckResult
import retrofit2.HttpException
import java.io.IOException

class ApiPasswordResetRepository(
    private val passwordResetApi: PasswordResetApi,
    private val errorResponseParser: ErrorResponseParser
) : PasswordResetRepository {

    override suspend fun requestReset(email: String): PasswordResetResult = try {
        passwordResetApi.requestReset(PasswordResetRequestDto(email.trim()))
        PasswordResetResult.Success
    } catch (exception: Exception) {
        PasswordResetResult.Failure(resetErrorOf(exception) ?: throw exception)
    }

    override suspend fun checkToken(token: String): ResetTokenCheckResult = try {
        passwordResetApi.checkToken(token).toResetTokenStatus()
            ?.let { ResetTokenCheckResult.Checked(it) }
            ?: ResetTokenCheckResult.Failure(PasswordResetError.UNKNOWN)
    } catch (exception: Exception) {
        ResetTokenCheckResult.Failure(resetErrorOf(exception) ?: throw exception)
    }

    override suspend fun resetPassword(token: String, password: String, confirmPassword: String): PasswordResetResult = try {
        passwordResetApi.confirm(PasswordResetConfirmRequestDto(token, password, confirmPassword))
        PasswordResetResult.Success
    } catch (exception: Exception) {
        PasswordResetResult.Failure(resetErrorOf(exception) ?: throw exception)
    }

    private fun resetErrorOf(exception: Exception): PasswordResetError? = when (exception) {
        is HttpException -> errorFromResponse(exception)
        is IOException -> PasswordResetError.NETWORK
        is IllegalArgumentException -> PasswordResetError.UNKNOWN
        else -> null
    }

    private fun errorFromResponse(exception: HttpException): PasswordResetError {
        val apiError = errorResponseParser.apiError(exception)
        return passwordResetErrorFrom(apiError?.code, apiError?.fieldNames.orEmpty())
    }
}
