package com.example.paypulse.ui.auth.passwordreset

import androidx.annotation.StringRes

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val isSent: Boolean = false,
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val formError: Int? = null
)
