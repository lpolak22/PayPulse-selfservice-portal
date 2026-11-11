package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.PasswordResetApiContract
import com.example.paypulse.domain.passwordreset.PasswordResetError

private val passwordResetErrorsByCode: Map<String, PasswordResetError> = mapOf(
    PasswordResetApiContract.CODE_TOKEN_EXPIRED to PasswordResetError.TOKEN_EXPIRED,
    PasswordResetApiContract.CODE_TOKEN_USED to PasswordResetError.TOKEN_USED,
    PasswordResetApiContract.CODE_TOKEN_INVALID to PasswordResetError.TOKEN_INVALID,
    PasswordResetApiContract.CODE_RATE_LIMITED to PasswordResetError.RATE_LIMITED
)

private val resetValidationErrorsByField: Map<String, PasswordResetError> = mapOf(
    PasswordResetApiContract.FIELD_EMAIL to PasswordResetError.INVALID_EMAIL,
    PasswordResetApiContract.FIELD_PASSWORD to PasswordResetError.PASSWORD_POLICY,
    PasswordResetApiContract.FIELD_CONFIRM_PASSWORD to PasswordResetError.PASSWORD_MISMATCH
)

fun passwordResetErrorFrom(code: String?, fieldNames: List<String> = emptyList()): PasswordResetError =
    if (code == PasswordResetApiContract.CODE_VALIDATION_ERROR) {
        resetValidationErrorFrom(fieldNames)
    } else {
        passwordResetErrorsByCode[code] ?: PasswordResetError.UNKNOWN
    }

private fun resetValidationErrorFrom(fieldNames: List<String>): PasswordResetError =
    resetValidationErrorsByField.entries.firstOrNull { it.key in fieldNames }?.value ?: PasswordResetError.UNKNOWN
