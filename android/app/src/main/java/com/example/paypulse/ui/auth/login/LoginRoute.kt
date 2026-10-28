package com.example.paypulse.ui.auth.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginRoute(
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) currentOnLoginSuccess()
    }
    LoginScreen(
        email = viewModel.email,
        password = viewModel.password,
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggle,
        onLoginClick = viewModel::onLoginClick,
        onForgotPasswordClick = onForgotPasswordClick,
        modifier = modifier
    )
}
