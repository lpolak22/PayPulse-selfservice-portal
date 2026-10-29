package com.example.paypulse.ui.users.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NewUserRoute(
    onBackClick: () -> Unit,
    onUserCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewUserViewModel = viewModel(factory = NewUserViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnUserCreated by rememberUpdatedState(onUserCreated)
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) currentOnUserCreated()
    }
    NewUserScreen(
        values = viewModel.input.values,
        state = state,
        onFieldChange = viewModel::onFieldChange,
        onRoleChange = viewModel::onRoleChange,
        onSaveClick = viewModel::onSaveClick,
        onBackClick = onBackClick,
        modifier = modifier
    )
}