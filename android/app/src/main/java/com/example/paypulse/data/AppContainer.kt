package com.example.paypulse.data

import com.example.paypulse.data.user.FakeUserRepository
import com.example.paypulse.domain.user.UserRepository

object AppContainer {
    val userRepository: UserRepository by lazy { FakeUserRepository() }
}