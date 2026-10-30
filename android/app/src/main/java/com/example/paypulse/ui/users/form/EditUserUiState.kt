package com.example.paypulse.ui.users.form

import com.example.paypulse.domain.user.User
import com.example.paypulse.domain.user.UserStatus

enum class EditUserDialog { DEACTIVATE, DELETE }

data class EditUserUiState(
    val user: User? = null,
    val form: UserFormUiState = UserFormUiState(),
    val status: UserStatus = UserStatus.ACTIVE,
    val isLoading: Boolean = true,
    val dialog: EditUserDialog? = null,
    val isDeleted: Boolean = false
) {
    val canToggleActive: Boolean
        get() = status != UserStatus.PENDING_ACTIVATION
}