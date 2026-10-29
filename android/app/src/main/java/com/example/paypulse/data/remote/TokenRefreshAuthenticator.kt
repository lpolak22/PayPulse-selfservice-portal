package com.example.paypulse.data.remote

import com.example.paypulse.data.auth.AuthTokens
import com.example.paypulse.data.auth.SessionStore
import com.example.paypulse.data.auth.TokenStorage
import com.example.paypulse.data.remote.dto.RefreshTokenRequestDto
import com.example.paypulse.data.remote.dto.TokenResponseDto
import com.example.paypulse.data.remote.mapper.loginErrorFromCode
import com.example.paypulse.data.remote.mapper.toAuthTokens
import com.example.paypulse.domain.auth.LoginError
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Response as RetrofitResponse

private val SESSION_ENDING_ERRORS = setOf(LoginError.SESSION_EXPIRED, LoginError.ACCOUNT_INACTIVE)

class TokenRefreshAuthenticator(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
    private val sessionStore: SessionStore,
    private val errorResponseParser: ErrorResponseParser
) : Authenticator {

    private val refreshMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.priorResponse != null) return null
        val failedAccessToken = response.request.bearerToken() ?: return null
        val accessToken = runBlocking {
            refreshMutex.withLock { currentOrRefreshedAccessToken(failedAccessToken) }
        }
        return accessToken?.let { response.request.withBearerToken(it) }
    }

    private suspend fun currentOrRefreshedAccessToken(failedAccessToken: String): String? {
        val storedTokens = tokenStorage.read() ?: return null
        if (storedTokens.accessToken != failedAccessToken) return storedTokens.accessToken
        return refresh(storedTokens.refreshToken)?.accessToken
    }

    private suspend fun refresh(refreshToken: String): AuthTokens? {
        val response = executeRefresh(refreshToken) ?: return null
        val body = response.body()
        if (response.isSuccessful && body != null) {
            return body.toAuthTokens().also { tokenStorage.save(it) }
        }
        val error = loginErrorFromCode(errorResponseParser.errorCode(response))
        if (error in SESSION_ENDING_ERRORS) endSession(error)
        return null
    }

    private suspend fun endSession(error: LoginError) {
        tokenStorage.clear()
        sessionStore.end(error)
    }

    private fun executeRefresh(refreshToken: String): RetrofitResponse<TokenResponseDto>? = try {
        authApi.refresh(RefreshTokenRequestDto(refreshToken)).execute()
    } catch (exception: SerializationException) {
        null
    }
}
