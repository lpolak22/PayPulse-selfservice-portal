package com.example.paypulse.ui.users.form

import androidx.annotation.StringRes
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