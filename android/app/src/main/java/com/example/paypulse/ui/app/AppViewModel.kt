package com.example.paypulse.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.auth.AuthRepository
import com.example.paypulse.domain.auth.SessionState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(authRepository: AuthRepository) : ViewModel() {

    val sessionState: StateFlow<SessionState> = authRepository.sessionState

    init {
        viewModelScope.launch { authRepository.restoreSession() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AppViewModel(AppContainer.authRepository) }
        }
    }
}
