package com.example.paypulse.domain.auth

import com.example.paypulse.domain.user.User

sealed interface SessionState {
    data object Checking : SessionState
    data class LoggedIn(val user: User) : SessionState
    data class LoggedOut(val error: LoginError? = null) : SessionState
}
