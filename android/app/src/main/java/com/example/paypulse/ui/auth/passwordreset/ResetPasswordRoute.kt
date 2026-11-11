package com.example.paypulse.ui.auth.passwordreset

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ResetPasswordRoute(
    viewModelKey: String,
    token: String,
    onReset: () -> Unit,
    onRequestNewLink: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResetPasswordViewModel = viewModel(key = viewModelKey, factory = ResetPasswordViewModel.factory(token))
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnReset by rememberUpdatedState(onReset)
    LaunchedEffect(state.isReset) {
        if (state.isReset) currentOnReset()
    }
    val leave = {
        viewModel.onLeave()
        onClose()
    }
    BackHandler(onBack = leave)
    ResetPasswordScreen(
        password = viewModel.password,
        confirmPassword = viewModel.confirmPassword,
        state = state,
        actions = ResetPasswordActions(
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggle,
            onConfirmPasswordVisibilityToggle = viewModel::onConfirmPasswordVisibilityToggle,
            onSubmitClick = viewModel::onSubmitClick,
            onRetryClick = viewModel::onRetryClick,
            onRequestNewLinkClick = {
                viewModel.onLeave()
                onRequestNewLink()
            },
            onBackToLoginClick = leave
        ),
        modifier = modifier
    )
}
