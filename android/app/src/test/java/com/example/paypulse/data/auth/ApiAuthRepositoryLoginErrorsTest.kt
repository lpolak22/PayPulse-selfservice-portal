package com.example.paypulse.data.auth

import com.example.paypulse.data.activation.CannedResponseInterceptor
import com.example.paypulse.data.remote.ErrorResponseParser
import com.example.paypulse.data.remote.createApiJson
import com.example.paypulse.data.remote.createRetrofit
import com.example.paypulse.domain.auth.LoginError
import com.example.paypulse.domain.auth.LoginResult
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.create
import java.io.IOException

private const val EMAIL = "korisnik@primjer.hr"
private const val PASSWORD = "Pogresna1"

class ApiAuthRepositoryLoginErrorsTest {

    private val server = CannedResponseInterceptor()
    private val json = createApiJson()
    private val retrofit = createRetrofit("http://localhost/", json, OkHttpClient.Builder().addInterceptor(server).build())
    private val tokenStorage = InMemoryTokenStorage()
    private val repository = ApiAuthRepository(
        retrofit.create(),
        retrofit.create(),
        ErrorResponseParser(json),
        tokenStorage,
        SessionStore()
    )

    private suspend fun loginAnswered(httpCode: Int, body: String): LoginResult {
        server.respond(httpCode, body)
        return repository.login(EMAIL, PASSWORD)
    }

    @Test
    fun wrongPassword_carriesTheRemainingAttempts() = runTest {
        val result = loginAnswered(401, """{"code":"INVALID_CREDENTIALS","message":"x","fieldErrors":[],"remainingAttempts":2}""")
        assertEquals(LoginResult.Failure(LoginError.INVALID_CREDENTIALS, remainingAttempts = 2), result)
    }

    @Test
    fun lockedAccount_carriesTheMinutesUntilTheLockEnds() = runTest {
        val result = loginAnswered(423, """{"code":"ACCOUNT_LOCKED","message":"x","fieldErrors":[],"retryAfterMinutes":15}""")
        assertEquals(LoginResult.Failure(LoginError.ACCOUNT_LOCKED, retryAfterMinutes = 15), result)
    }

    @Test
    fun lockedAccountFromAnOlderServer_isStillLocked() = runTest {
        val result = loginAnswered(423, """{"code":"ACCOUNT_LOCKED","message":"x","fieldErrors":[]}""")
        assertEquals(LoginResult.Failure(LoginError.ACCOUNT_LOCKED), result)
    }

    @Test
    fun otherErrors_carryNoCounts() = runTest {
        val result = loginAnswered(403, """{"code":"ACCOUNT_INACTIVE","message":"x","fieldErrors":[]}""")
        assertEquals(LoginResult.Failure(LoginError.ACCOUNT_INACTIVE), result)
    }

    @Test
    fun unreadableErrorBody_isUnknown() = runTest {
        assertEquals(LoginResult.Failure(LoginError.UNKNOWN), loginAnswered(500, "not json"))
    }

    @Test
    fun offline_isNetworkError() = runTest {
        server.failure = IOException("offline")
        assertEquals(LoginResult.Failure(LoginError.NETWORK), repository.login(EMAIL, PASSWORD))
    }

    @Test
    fun failedLogin_storesNoTokens() = runTest {
        loginAnswered(423, """{"code":"ACCOUNT_LOCKED","message":"x","fieldErrors":[],"retryAfterMinutes":15}""")
        assertNull(tokenStorage.read())
    }

    private class InMemoryTokenStorage : TokenStorage {
        private var tokens: AuthTokens? = null

        override suspend fun read(): AuthTokens? = tokens

        override suspend fun save(tokens: AuthTokens) {
            this.tokens = tokens
        }

        override suspend fun clear() {
            tokens = null
        }
    }
}
