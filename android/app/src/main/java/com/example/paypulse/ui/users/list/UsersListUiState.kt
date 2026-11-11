package com.example.paypulse.ui.users.list

import com.example.paypulse.domain.user.User

data class UsersListUiState(
    val isLoading: Boolean = true,
    val users: List<User> = emptyList(),
    val totalCount: Int = 0
)