package com.example.paypulse.domain.user

import com.example.paypulse.domain.auth.EmailRule

enum class UserField { FIRST_NAME, LAST_NAME, EMAIL, PHONE }

enum class FieldError { REQUIRED, TOO_LONG, INVALID_EMAIL, INVALID_PHONE }

object UserDraftValidator {
    private const val NAME_MAX_LENGTH = 50
    private const val EMAIL_MAX_LENGTH = 254
    private val phonePattern = Regex("^\\+[1-9]\\d{1,14}$")

    fun normalizePhone(phone: String): String? =
        phone.filterNot { it.isWhitespace() }.ifEmpty { null }

    fun validate(draft: UserDraft): Map<UserField, FieldError> = buildMap {
        nameError(draft.firstName)?.let { put(UserField.FIRST_NAME, it) }
        nameError(draft.lastName)?.let { put(UserField.LAST_NAME, it) }
        emailError(draft.email)?.let { put(UserField.EMAIL, it) }
        phoneError(draft.phone)?.let { put(UserField.PHONE, it) }
    }

    private fun nameError(name: String): FieldError? = when {
        name.isBlank() -> FieldError.REQUIRED
        name.length > NAME_MAX_LENGTH -> FieldError.TOO_LONG
        else -> null
    }

    private fun emailError(email: String): FieldError? = when {
        email.isBlank() -> FieldError.REQUIRED
        email.length > EMAIL_MAX_LENGTH -> FieldError.TOO_LONG
        !EmailRule.isValid(email) -> FieldError.INVALID_EMAIL
        else -> null
    }

    private fun phoneError(phone: String?): FieldError? =
        if (phone == null || phonePattern.matches(phone)) null else FieldError.INVALID_PHONE
}