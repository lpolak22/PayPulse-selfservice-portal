package com.example.paypulse.data.auth

import com.example.paypulse.domain.auth.LoginError
import com.example.paypulse.domain.auth.SessionState
import com.example.paypulse.domain.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionStore {
    private val _state = MutableStateFlow<SessionState>(SessionState.Checking)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    fun start(user: User) {
        _state.value = SessionState.LoggedIn(user)
    }

    fun end(error: LoginError? = null) {
        _state.value = SessionState.LoggedOut(error)
    }
}
