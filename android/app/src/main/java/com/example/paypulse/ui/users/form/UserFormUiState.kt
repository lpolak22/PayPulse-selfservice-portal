package com.example.paypulse.ui.users.form

import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.user.EmailAlreadyExistsException
import com.example.paypulse.domain.user.FieldError
import com.example.paypulse.domain.user.LastActiveAdminException
import com.example.paypulse.domain.user.UserField
import com.example.paypulse.domain.user.UserRole

data class UserFormUiState(
    val role: UserRole = UserRole.USER,
    @StringRes val firstNameError: Int? = null,
    @StringRes val lastNameError: Int? = null,
    @StringRes val emailError: Int? = null,
    @StringRes val phoneError: Int? = null,
    @StringRes val formError: Int? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)

internal fun UserFormUiState.withoutError(field: UserField): UserFormUiState = when (field) {
    UserField.FIRST_NAME -> copy(firstNameError = null, formError = null)
    UserField.LAST_NAME -> copy(lastNameError = null, formError = null)
    UserField.EMAIL -> copy(emailError = null, formError = null)
    UserField.PHONE -> copy(phoneError = null, formError = null)
}

internal fun UserFormUiState.withFieldErrors(errors: Map<UserField, FieldError>): UserFormUiState = copy(
    firstNameError = errors[UserField.FIRST_NAME]?.toMessage(),
    lastNameError = errors[UserField.LAST_NAME]?.toMessage(),
    emailError = errors[UserField.EMAIL]?.toMessage(),
    phoneError = errors[UserField.PHONE]?.toMessage(),
    formError = null
)

internal fun UserFormUiState.withSaveError(error: Exception): UserFormUiState = when (error) {
    is EmailAlreadyExistsException -> copy(isSaving = false, emailError = R.string.users_error_email_exists)
    is LastActiveAdminException -> copy(isSaving = false, formError = R.string.users_error_last_admin)
    else -> copy(isSaving = false, formError = R.string.users_error_save_failed)
}