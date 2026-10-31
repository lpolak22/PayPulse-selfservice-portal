package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.ActivationApiContract
import com.example.paypulse.domain.activation.ActivationError

private val activationErrorsByCode: Map<String, ActivationError> = mapOf(
    ActivationApiContract.CODE_TOKEN_EXPIRED to ActivationError.TOKEN_EXPIRED,
    ActivationApiContract.CODE_TOKEN_USED to ActivationError.TOKEN_USED,
    ActivationApiContract.CODE_TOKEN_INVALID to ActivationError.TOKEN_INVALID
)

private val validationErrorsByField: Map<String, ActivationError> = mapOf(
    ActivationApiContract.FIELD_PASSWORD to ActivationError.PASSWORD_POLICY,
    ActivationApiContract.FIELD_CONFIRM_PASSWORD to ActivationError.PASSWORD_MISMATCH
)

fun activationErrorFrom(code: String?, fieldNames: List<String> = emptyList()): ActivationError =
    if (code == ActivationApiContract.CODE_VALIDATION_ERROR) {
        validationErrorFrom(fieldNames)
    } else {
        activationErrorsByCode[code] ?: ActivationError.UNKNOWN
    }

private fun validationErrorFrom(fieldNames: List<String>): ActivationError =
    validationErrorsByField.entries.firstOrNull { it.key in fieldNames }?.value ?: ActivationError.UNKNOWN
