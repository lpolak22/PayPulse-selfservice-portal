package com.example.paypulse.ui.shell

import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.user.UserRole

@StringRes
fun UserRole.toLabel(): Int = when (this) {
    UserRole.ADMIN -> R.string.navigation_role_admin
    UserRole.USER -> R.string.navigation_role_user
}
