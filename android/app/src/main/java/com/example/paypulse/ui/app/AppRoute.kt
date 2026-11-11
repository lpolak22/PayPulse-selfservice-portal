package com.example.paypulse.ui.app

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paypulse.domain.auth.SessionState
import com.example.paypulse.ui.auth.activation.ActivationRoute
import com.example.paypulse.ui.auth.login.LoginRoute
import com.example.paypulse.ui.auth.passwordreset.ForgotPasswordRoute
import com.example.paypulse.ui.components.LoadingScreen
import com.example.paypulse.ui.shell.AppShell

@Composable
fun AppRoute(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val activationRequest by viewModel.activationRequest.collectAsStateWithLifecycle()
    val loginNotice by viewModel.loginNotice.collectAsStateWithLifecycle()
    val forgotPasswordId by viewModel.forgotPasswordId.collectAsStateWithLifecycle()
    val request = activationRequest
    if (request != null) {
        ActivationRoute(
            viewModelKey = "activation-${request.id}",
            token = request.token,
            onActivated = viewModel::onActivationSucceeded,
            onClose = viewModel::onActivationClosed,
            modifier = modifier
        )
    } else {
        SessionContent(
            sessionState = sessionState,
            loginNotice = loginNotice,
            forgotPasswordId = forgotPasswordId,
            actions = SessionActions(
                onLogoutClick = viewModel::onLogoutClick,
                onForgotPasswordClick = viewModel::onForgotPasswordClick,
                onForgotPasswordClose = viewModel::onForgotPasswordClosed
            ),
            modifier = modifier
        )
    }
}

private class SessionActions(
    val onLogoutClick: () -> Unit,
    val onForgotPasswordClick: () -> Unit,
    val onForgotPasswordClose: () -> Unit
)

@Composable
private fun SessionContent(
    sessionState: SessionState,
    @StringRes loginNotice: Int?,
    forgotPasswordId: Int?,
    actions: SessionActions,
    modifier: Modifier = Modifier
) {
    when (sessionState) {
        SessionState.Checking -> LoadingScreen(modifier = modifier)
        is SessionState.LoggedIn -> AppShell(
            user = sessionState.user,
            onLogoutClick = actions.onLogoutClick,
            modifier = modifier
        )
        is SessionState.LoggedOut -> if (forgotPasswordId != null) {
            ForgotPasswordRoute(
                viewModelKey = "forgot-password-$forgotPasswordId",
                onClose = actions.onForgotPasswordClose,
                modifier = modifier
            )
        } else {
            LoginRoute(
                onForgotPasswordClick = actions.onForgotPasswordClick,
                notice = loginNotice,
                modifier = modifier
            )
        }
    }
}
