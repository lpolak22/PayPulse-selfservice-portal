package com.example.paypulse.ui.auth.passwordreset

import androidx.annotation.StringRes
import com.example.paypulse.domain.auth.PasswordRule

data class ResetPasswordUiState(
    val page: ResetPasswordPage = ResetPasswordPage.CheckingLink,
    val metRules: Set<PasswordRule> = emptySet(),
    val highlightUnmetRules: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isReset: Boolean = false,
    @param:StringRes val confirmPasswordError: Int? = null,
    @param:StringRes val formError: Int? = null
)
