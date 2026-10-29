package com.example.paypulse.data.remote.mapper

import com.example.paypulse.domain.auth.LoginError

private val loginErrorsByCode: Map<String, LoginError> = mapOf(
    "INVALID_CREDENTIALS" to LoginError.INVALID_CREDENTIALS,
    "ACCOUNT_INACTIVE" to LoginError.ACCOUNT_INACTIVE,
    "ACCOUNT_PENDING_ACTIVATION" to LoginError.ACCOUNT_PENDING_ACTIVATION
)

fun loginErrorFromCode(code: String?): LoginError = loginErrorsByCode[code] ?: LoginError.UNKNOWN
