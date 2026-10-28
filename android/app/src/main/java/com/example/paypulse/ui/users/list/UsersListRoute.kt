package com.example.paypulse.ui.users.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun UsersListRoute(
    onAddUser: () -> Unit,
    onUserClick: (Long) -> Unit,
    viewModel: UsersListViewModel = viewModel(factory = UsersListViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    UsersListScreen(
        state = state,
        query = viewModel.query,
        onQueryChange = viewModel::onQueryChange,
        onAddUser = onAddUser,
        onUserClick = onUserClick
    )
}