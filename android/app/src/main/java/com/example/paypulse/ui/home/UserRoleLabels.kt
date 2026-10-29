package com.example.paypulse.ui.home

import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.user.UserRole

@StringRes
fun UserRole.toLabel(): Int = when (this) {
    UserRole.ADMIN -> R.string.home_role_admin
    UserRole.USER -> R.string.home_role_user
}
