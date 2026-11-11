package com.example.paypulse.ui.auth.passwordreset

import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.passwordreset.PasswordResetError

@StringRes
fun PasswordResetError.toForgotPasswordMessage(): Int = when (this) {
    PasswordResetError.RATE_LIMITED -> R.string.forgot_password_error_rate_limited
    PasswordResetError.NETWORK -> R.string.forgot_password_error_network
    else -> R.string.forgot_password_error_unknown
}
