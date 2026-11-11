package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.ApiError
import com.example.paypulse.domain.auth.LoginError
import com.example.paypulse.domain.auth.LoginResult

private val loginErrorsByCode: Map<String, LoginError> = mapOf(
    "INVALID_CREDENTIALS" to LoginError.INVALID_CREDENTIALS,
    "ACCOUNT_LOCKED" to LoginError.ACCOUNT_LOCKED,
    "ACCOUNT_INACTIVE" to LoginError.ACCOUNT_INACTIVE,
    "ACCOUNT_PENDING_ACTIVATION" to LoginError.ACCOUNT_PENDING_ACTIVATION,
    "INVALID_REFRESH_TOKEN" to LoginError.SESSION_EXPIRED
)

fun loginErrorFromCode(code: String?): LoginError = loginErrorsByCode[code] ?: LoginError.UNKNOWN

fun loginFailureFrom(apiError: ApiError?): LoginResult.Failure = LoginResult.Failure(
    error = loginErrorFromCode(apiError?.code),
    remainingAttempts = apiError?.remainingAttempts,
    retryAfterMinutes = apiError?.retryAfterMinutes
)
