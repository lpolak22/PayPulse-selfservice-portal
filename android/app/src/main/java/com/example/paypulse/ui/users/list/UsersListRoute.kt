package com.example.paypulse.ui.users.list

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun UsersListRoute(
    onAddUser: () -> Unit,
    onUserClick: (Long) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: UsersListViewModel = viewModel(factory = UsersListViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    UsersListScreen(
        state = state,
        query = viewModel.query,
        onQueryChange = viewModel::onQueryChange,
        onAddUser = onAddUser,
        onUserClick = onUserClick,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    )
}