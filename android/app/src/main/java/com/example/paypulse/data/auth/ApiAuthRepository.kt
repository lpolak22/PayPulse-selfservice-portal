package com.example.paypulse.data.auth

import com.example.paypulse.data.remote.AuthApi
import com.example.paypulse.data.remote.CurrentUserApi
import com.example.paypulse.data.remote.ErrorResponseParser
import com.example.paypulse.data.remote.dto.LoginRequestDto
import com.example.paypulse.data.remote.dto.RefreshTokenRequestDto
import com.example.paypulse.data.remote.mapper.loginErrorFromCode
import com.example.paypulse.data.remote.mapper.toAuthTokens
import com.example.paypulse.data.remote.mapper.toUser
import com.example.paypulse.domain.auth.AuthRepository
import com.example.paypulse.domain.auth.LoginError
import com.example.paypulse.domain.auth.LoginResult
import com.example.paypulse.domain.auth.SessionState
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import java.io.IOException
import java.security.GeneralSecurityException
import java.time.DateTimeException

class ApiAuthRepository(
    private val authApi: AuthApi,
    private val currentUserApi: CurrentUserApi,
    private val errorResponseParser: ErrorResponseParser,
    private val tokenStorage: TokenStorage,
    private val sessionStore: SessionStore
) : AuthRepository {

    override val sessionState: StateFlow<SessionState> = sessionStore.state

    override suspend fun restoreSession() {
        if (tokenStorage.read() == null) {
            sessionStore.end()
            return
        }
        val error = startSession() ?: return
        if (sessionState.value == SessionState.Checking) sessionStore.end(error)
    }

    override suspend fun login(email: String, password: String): LoginResult {
        requestTokens(email, password)?.let { return LoginResult.Failure(it) }
        val sessionError = startSession() ?: return LoginResult.Success
        tokenStorage.clear()
        return LoginResult.Failure(sessionError)
    }

    override suspend fun logout() {
        val refreshToken = tokenStorage.read()?.refreshToken
        tokenStorage.clear()
        sessionStore.end()
        if (refreshToken != null) revokeOnServer(refreshToken)
    }

    private suspend fun revokeOnServer(refreshToken: String): Boolean = try {
        authApi.logout(RefreshTokenRequestDto(refreshToken)).isSuccessful
    } catch (exception: IOException) {
        false
    }

    private suspend fun requestTokens(email: String, password: String): LoginError? = loginErrorOrNull {
        tokenStorage.save(authApi.login(LoginRequestDto(email, password)).toAuthTokens())
    }

    private suspend fun startSession(): LoginError? = loginErrorOrNull {
        sessionStore.start(currentUserApi.me().toUser())
    }

    private suspend fun loginErrorOrNull(request: suspend () -> Unit): LoginError? = try {
        request()
        null
    } catch (exception: HttpException) {
        loginErrorFromCode(errorResponseParser.errorCode(exception))
    } catch (exception: IOException) {
        LoginError.NETWORK
    } catch (exception: IllegalArgumentException) {
        LoginError.UNKNOWN
    } catch (exception: DateTimeException) {
        LoginError.UNKNOWN
    } catch (exception: GeneralSecurityException) {
        LoginError.UNKNOWN
    }
}
