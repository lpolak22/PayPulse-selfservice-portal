package com.example.paypulse.ui.auth.activation

import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.activation.ActivationError
import com.example.paypulse.domain.activation.ActivationTokenStatus

fun ActivationTokenStatus.toPage(): ActivationPage = when (this) {
    ActivationTokenStatus.VALID -> ActivationPage.Form
    ActivationTokenStatus.EXPIRED -> ActivationPage.LinkProblem(R.string.activation_link_expired)
    ActivationTokenStatus.USED -> ActivationPage.LinkProblem(R.string.activation_link_used)
}

fun ActivationError.toLinkCheckProblem(): ActivationPage.LinkProblem =
    if (this == ActivationError.NETWORK) {
        ActivationPage.LinkProblem(R.string.activation_link_check_failed, canRetry = true)
    } else {
        toLinkProblemOrNull() ?: ActivationPage.LinkProblem(R.string.activation_link_invalid)
    }

fun ActivationError.toLinkProblemOrNull(): ActivationPage.LinkProblem? = when (this) {
    ActivationError.TOKEN_EXPIRED -> ActivationPage.LinkProblem(R.string.activation_link_expired)
    ActivationError.TOKEN_USED -> ActivationPage.LinkProblem(R.string.activation_link_used)
    ActivationError.TOKEN_INVALID -> ActivationPage.LinkProblem(R.string.activation_link_invalid)
    else -> null
}

@StringRes
fun ActivationError.toFormMessage(): Int = when (this) {
    ActivationError.PASSWORD_POLICY -> R.string.activation_error_password_policy
    ActivationError.NETWORK -> R.string.activation_error_network
    else -> R.string.activation_error_unknown
}
