package com.example.paypulse.ui.auth.activation

import androidx.annotation.StringRes

sealed interface ActivationPage {
    data object CheckingLink : ActivationPage
    data object Form : ActivationPage
    data class LinkProblem(
        @param:StringRes val message: Int,
        val canRetry: Boolean = false
    ) : ActivationPage
}
