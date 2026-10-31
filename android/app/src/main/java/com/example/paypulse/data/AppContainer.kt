package com.example.paypulse.data

import android.content.Context
import com.example.paypulse.BuildConfig
import com.example.paypulse.data.activation.FakeActivationRepository
import com.example.paypulse.data.auth.ApiAuthRepository
import com.example.paypulse.data.auth.EncryptedTokenStorage
import com.example.paypulse.data.auth.SessionStore
import com.example.paypulse.data.auth.TokenStorage
import com.example.paypulse.data.remote.AuthApi
import com.example.paypulse.data.remote.BearerTokenInterceptor
import com.example.paypulse.data.remote.CurrentUserApi
import com.example.paypulse.data.remote.ErrorResponseParser
import com.example.paypulse.data.remote.TokenRefreshAuthenticator
import com.example.paypulse.data.remote.createApiJson
import com.example.paypulse.data.remote.createOkHttpClient
import com.example.paypulse.data.remote.createRetrofit
import com.example.paypulse.data.remote.withTokenAuthentication
import com.example.paypulse.data.security.KeystoreCipher
import com.example.paypulse.data.user.FakeUserRepository
import com.example.paypulse.domain.activation.ActivationRepository
import com.example.paypulse.domain.auth.AuthRepository
import com.example.paypulse.domain.user.UserRepository
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create

object AppContainer {
    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    private val json: Json by lazy { createApiJson() }
    private val errorResponseParser: ErrorResponseParser by lazy { ErrorResponseParser(json) }
    private val httpClient: OkHttpClient by lazy { createOkHttpClient() }
    private val authApi: AuthApi by lazy { createRetrofit(BuildConfig.API_BASE_URL, json, httpClient).create() }
    private val tokenStorage: TokenStorage by lazy { EncryptedTokenStorage(applicationContext, KeystoreCipher()) }
    private val sessionStore: SessionStore by lazy { SessionStore() }

    private val authenticatedRetrofit: Retrofit by lazy {
        val client = httpClient.withTokenAuthentication(
            BearerTokenInterceptor(tokenStorage),
            TokenRefreshAuthenticator(authApi, tokenStorage, sessionStore, errorResponseParser)
        )
        createRetrofit(BuildConfig.API_BASE_URL, json, client)
    }
    private val currentUserApi: CurrentUserApi by lazy { authenticatedRetrofit.create() }

    val authRepository: AuthRepository by lazy {
        ApiAuthRepository(authApi, currentUserApi, errorResponseParser, tokenStorage, sessionStore)
    }

    val activationRepository: ActivationRepository by lazy { FakeActivationRepository() }
    val userRepository: UserRepository by lazy { FakeUserRepository() }
}
