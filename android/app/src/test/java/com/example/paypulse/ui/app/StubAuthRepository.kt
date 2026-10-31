package com.example.paypulse.ui.app

import com.example.paypulse.domain.auth.AuthRepository
import com.example.paypulse.domain.auth.LoginResult
import com.example.paypulse.domain.auth.SessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StubAuthRepository : AuthRepository {

    val session = MutableStateFlow<SessionState>(SessionState.LoggedOut())

    override val sessionState: StateFlow<SessionState> = session

    override suspend fun restoreSession() = Unit

    override suspend fun login(email: String, password: String): LoginResult = LoginResult.Success

    override suspend fun logout() = Unit
}
