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
import com.example.paypulse.ui.components.LoadingScreen
import com.example.paypulse.ui.home.HomeScreen

@Composable
fun AppRoute(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val activationRequest by viewModel.activationRequest.collectAsStateWithLifecycle()
    val loginNotice by viewModel.loginNotice.collectAsStateWithLifecycle()
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
        SessionContent(sessionState, loginNotice, viewModel::onLogoutClick, modifier)
    }
}

@Composable
private fun SessionContent(
    sessionState: SessionState,
    @StringRes loginNotice: Int?,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (sessionState) {
        SessionState.Checking -> LoadingScreen(modifier = modifier)
        is SessionState.LoggedIn -> HomeScreen(
            user = sessionState.user,
            onLogoutClick = onLogoutClick,
            modifier = modifier
        )
        is SessionState.LoggedOut -> LoginRoute(
            onForgotPasswordClick = {},
            notice = loginNotice,
            modifier = modifier
        )
    }
}
