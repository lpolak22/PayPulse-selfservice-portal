package com.example.paypulse.ui.auth.passwordreset

import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.passwordreset.PasswordResetError
import com.example.paypulse.domain.passwordreset.ResetTokenStatus

fun ResetTokenStatus.toPage(): ResetPasswordPage = when (this) {
    ResetTokenStatus.VALID -> ResetPasswordPage.Form
    ResetTokenStatus.EXPIRED -> ResetPasswordPage.LinkProblem(R.string.reset_password_link_expired)
    ResetTokenStatus.USED -> ResetPasswordPage.LinkProblem(R.string.reset_password_link_used)
}

fun PasswordResetError.toLinkCheckProblem(): ResetPasswordPage.LinkProblem =
    if (this == PasswordResetError.NETWORK) {
        ResetPasswordPage.LinkProblem(R.string.reset_password_link_check_failed, canRetry = true)
    } else {
        toLinkProblemOrNull() ?: ResetPasswordPage.LinkProblem(R.string.reset_password_link_invalid)
    }

fun PasswordResetError.toLinkProblemOrNull(): ResetPasswordPage.LinkProblem? = when (this) {
    PasswordResetError.TOKEN_EXPIRED -> ResetPasswordPage.LinkProblem(R.string.reset_password_link_expired)
    PasswordResetError.TOKEN_USED -> ResetPasswordPage.LinkProblem(R.string.reset_password_link_used)
    PasswordResetError.TOKEN_INVALID -> ResetPasswordPage.LinkProblem(R.string.reset_password_link_invalid)
    else -> null
}

@StringRes
fun PasswordResetError.toResetFormMessage(): Int = when (this) {
    PasswordResetError.PASSWORD_POLICY -> R.string.reset_password_error_password_policy
    PasswordResetError.NETWORK -> R.string.reset_password_error_network
    else -> R.string.reset_password_error_unknown
}
