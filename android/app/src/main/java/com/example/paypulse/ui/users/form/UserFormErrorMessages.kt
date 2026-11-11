package com.example.paypulse.ui.users.form

import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.user.FieldError

@StringRes
internal fun FieldError.toMessage(): Int = when (this) {
    FieldError.REQUIRED -> R.string.users_error_required
    FieldError.TOO_LONG -> R.string.users_error_too_long
    FieldError.INVALID_EMAIL -> R.string.users_error_email_invalid
    FieldError.INVALID_PHONE -> R.string.users_error_phone_invalid
}