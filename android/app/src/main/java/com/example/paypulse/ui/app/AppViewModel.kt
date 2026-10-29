package com.example.paypulse.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.R
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.auth.AuthRepository
import com.example.paypulse.domain.auth.SessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val sessionState: StateFlow<SessionState> = authRepository.sessionState

    private val _activationRequest = MutableStateFlow<ActivationRequest?>(null)
    val activationRequest: StateFlow<ActivationRequest?> = _activationRequest.asStateFlow()

    private val _loginNotice = MutableStateFlow<Int?>(null)
    val loginNotice: StateFlow<Int?> = _loginNotice.asStateFlow()

    private var lastActivationId = 0

    init {
        viewModelScope.launch { authRepository.restoreSession() }
        viewModelScope.launch {
            sessionState.collect { if (it is SessionState.LoggedIn) _loginNotice.value = null }
        }
    }

    fun onActivationLink(link: String?) {
        val token = activationTokenFrom(link) ?: return
        _loginNotice.value = null
        _activationRequest.value = ActivationRequest(id = ++lastActivationId, token = token)
    }

    fun onActivationSucceeded() {
        _activationRequest.value = null
        if (sessionState.value !is SessionState.LoggedIn) _loginNotice.value = R.string.activation_success_notice
    }

    fun onActivationClosed() {
        _activationRequest.value = null
    }

    fun onLogoutClick() {
        viewModelScope.launch { authRepository.logout() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AppViewModel(AppContainer.authRepository) }
        }
    }
}
