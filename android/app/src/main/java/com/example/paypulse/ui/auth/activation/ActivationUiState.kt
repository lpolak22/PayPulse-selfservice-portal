package com.example.paypulse.ui.auth.activation

import androidx.annotation.StringRes
import com.example.paypulse.domain.auth.PasswordRule

data class ActivationUiState(
    val page: ActivationPage = ActivationPage.CheckingLink,
    val metRules: Set<PasswordRule> = emptySet(),
    val highlightUnmetRules: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    @param:StringRes val confirmPasswordError: Int? = null,
    @param:StringRes val formError: Int? = null
)
