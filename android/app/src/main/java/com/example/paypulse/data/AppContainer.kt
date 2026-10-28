package com.example.paypulse.data

import com.example.paypulse.data.auth.FakeAuthRepository
import com.example.paypulse.domain.auth.AuthRepository

object AppContainer {
    val authRepository: AuthRepository by lazy { FakeAuthRepository() }
}
