package com.example.paypulse.ui.auth.activation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ActivationRoute(
    viewModelKey: String,
    token: String,
    onActivated: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivationViewModel = viewModel(key = viewModelKey, factory = ActivationViewModel.factory(token))
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnActivated by rememberUpdatedState(onActivated)
    LaunchedEffect(state.isActivated) {
        if (state.isActivated) currentOnActivated()
    }
    val leave = {
        viewModel.onLeave()
        onClose()
    }
    BackHandler(onBack = leave)
    ActivationScreen(
        password = viewModel.password,
        confirmPassword = viewModel.confirmPassword,
        state = state,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggle,
        onConfirmPasswordVisibilityToggle = viewModel::onConfirmPasswordVisibilityToggle,
        onSubmitClick = viewModel::onSubmitClick,
        onRetryClick = viewModel::onRetryClick,
        onBackToLoginClick = leave,
        modifier = modifier
    )
}
