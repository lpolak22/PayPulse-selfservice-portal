package com.example.paypulse.ui.auth.login

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginRoute(
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes notice: Int? = null,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LoginScreen(
        email = viewModel.email,
        password = viewModel.password,
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggle,
        onLoginClick = viewModel::onLoginClick,
        onForgotPasswordClick = onForgotPasswordClick,
        modifier = modifier,
        notice = notice
    )
}
