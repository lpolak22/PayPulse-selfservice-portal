package com.example.paypulse.ui.auth.passwordreset

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ForgotPasswordRoute(
    viewModelKey: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = viewModel(key = viewModelKey, factory = ForgotPasswordViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(onBack = onClose)
    ForgotPasswordScreen(
        email = viewModel.email,
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onSubmitClick = viewModel::onSubmitClick,
        onBackToLoginClick = onClose,
        modifier = modifier
    )
}
