package com.example.paypulse.ui.auth.passwordreset

import androidx.annotation.StringRes

sealed interface ResetPasswordPage {
    data object CheckingLink : ResetPasswordPage
    data object Form : ResetPasswordPage
    data class LinkProblem(
        @param:StringRes val message: Int,
        val canRetry: Boolean = false
    ) : ResetPasswordPage
}
