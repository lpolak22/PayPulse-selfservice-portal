package com.example.paypulse.ui.auth.login

import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.auth.LoginError
import com.example.paypulse.domain.auth.LoginResult

@StringRes
fun LoginError.toMessage(): Int = when (this) {
    LoginError.INVALID_CREDENTIALS -> R.string.login_error_invalid_credentials
    LoginError.ACCOUNT_LOCKED -> R.string.login_error_account_locked
    LoginError.ACCOUNT_INACTIVE -> R.string.login_error_account_inactive
    LoginError.ACCOUNT_PENDING_ACTIVATION -> R.string.login_error_account_pending_activation
    LoginError.SESSION_EXPIRED -> R.string.login_error_session_expired
    LoginError.NETWORK -> R.string.login_error_network
    LoginError.UNKNOWN -> R.string.login_error_unknown
}

fun LoginError.toFormError(): LoginFormError = LoginFormError.Message(toMessage())

fun LoginResult.Failure.toFormError(): LoginFormError = when {
    error == LoginError.INVALID_CREDENTIALS && remainingAttempts != null ->
        LoginFormError.InvalidCredentials(remainingAttempts)
    error == LoginError.ACCOUNT_LOCKED && retryAfterMinutes != null ->
        LoginFormError.AccountLocked(retryAfterMinutes)
    else -> error.toFormError()
}
