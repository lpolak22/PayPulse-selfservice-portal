package com.example.paypulse.ui.users.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EditUserRoute(
    userId: Long,
    onBackClick: () -> Unit,
    onUserSaved: () -> Unit,
    onUserDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditUserViewModel = viewModel(factory = EditUserViewModel.factory(userId))
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnUserSaved by rememberUpdatedState(onUserSaved)
    val currentOnUserDeleted by rememberUpdatedState(onUserDeleted)
    LaunchedEffect(state.form.isSaved, state.isDeleted) {
        if (state.form.isSaved) currentOnUserSaved()
        if (state.isDeleted) currentOnUserDeleted()
    }
    EditUserScreen(
        values = viewModel.input.values,
        state = state,
        onFieldChange = viewModel::onFieldChange,
        onRoleChange = viewModel::onRoleChange,
        onActiveChange = viewModel::onActiveChange,
        onSaveClick = viewModel::onSaveClick,
        onDeleteClick = viewModel::onDeleteClick,
        onDialogConfirm = viewModel::onDialogConfirm,
        onDialogDismiss = viewModel::onDialogDismiss,
        onBackClick = onBackClick,
        modifier = modifier
    )
}