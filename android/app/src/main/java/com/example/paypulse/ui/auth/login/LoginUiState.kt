package com.example.paypulse.ui.auth.login

import androidx.annotation.StringRes

data class LoginUiState(
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
    @param:StringRes val formError: Int? = null
)
