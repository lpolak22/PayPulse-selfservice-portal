package com.example.paypulse.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paypulse.domain.auth.SessionState
import com.example.paypulse.ui.auth.login.LoginRoute
import com.example.paypulse.ui.components.LoadingScreen
import com.example.paypulse.ui.home.HomeScreen

@Composable
fun AppRoute(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    when (val state = sessionState) {
        SessionState.Checking -> LoadingScreen(modifier = modifier)
        is SessionState.LoggedIn -> HomeScreen(
            user = state.user,
            onLogoutClick = viewModel::onLogoutClick,
            modifier = modifier
        )
        is SessionState.LoggedOut -> LoginRoute(onForgotPasswordClick = {}, modifier = modifier)
    }
}
