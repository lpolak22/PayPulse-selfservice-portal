package com.example.paypulse.ui.auth.login

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.paypulse.R

sealed interface LoginFormError {
    data class Message(@param:StringRes val text: Int) : LoginFormError
    data class InvalidCredentials(val remainingAttempts: Int) : LoginFormError
    data class AccountLocked(val retryAfterMinutes: Int) : LoginFormError
}

@Composable
fun LoginFormError.text(): String = when (this) {
    is LoginFormError.Message -> stringResource(text)
    is LoginFormError.InvalidCredentials -> pluralStringResource(
        R.plurals.login_error_invalid_credentials_remaining,
        remainingAttempts,
        remainingAttempts
    )
    is LoginFormError.AccountLocked -> pluralStringResource(
        R.plurals.login_error_account_locked_minutes,
        retryAfterMinutes,
        retryAfterMinutes
    )
}
