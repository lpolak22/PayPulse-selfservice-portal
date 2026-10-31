package com.example.paypulse.ui.auth.login

import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.auth.LoginError

@StringRes
fun LoginError.toMessage(): Int = when (this) {
    LoginError.INVALID_CREDENTIALS -> R.string.login_error_invalid_credentials
    LoginError.ACCOUNT_INACTIVE -> R.string.login_error_account_inactive
    LoginError.ACCOUNT_PENDING_ACTIVATION -> R.string.login_error_account_pending_activation
    LoginError.SESSION_EXPIRED -> R.string.login_error_session_expired
    LoginError.NETWORK -> R.string.login_error_network
    LoginError.UNKNOWN -> R.string.login_error_unknown
}
