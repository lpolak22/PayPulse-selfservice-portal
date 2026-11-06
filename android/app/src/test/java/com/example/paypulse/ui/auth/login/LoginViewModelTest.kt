package com.example.paypulse.ui.auth.login

import com.example.paypulse.MainDispatcherRule
import com.example.paypulse.R
import com.example.paypulse.domain.auth.LoginError
import com.example.paypulse.domain.auth.LoginResult
import com.example.paypulse.ui.app.StubAuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

private const val EMAIL = "korisnik@primjer.hr"
private const val PASSWORD = "Pogresna1"

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = StubAuthRepository()
    private val viewModel by lazy { LoginViewModel(repository) }

    private fun TestScope.loginAnswered(result: LoginResult): LoginFormError? {
        repository.loginResult = result
        viewModel.onEmailChange(EMAIL)
        viewModel.onPasswordChange(PASSWORD)
        viewModel.onLoginClick()
        advanceUntilIdle()
        return viewModel.uiState.value.formError
    }

    @Test
    fun wrongPassword_showsTheRemainingAttempts() = runTest {
        val formError = loginAnswered(LoginResult.Failure(LoginError.INVALID_CREDENTIALS, remainingAttempts = 2))
        assertEquals(LoginFormError.InvalidCredentials(2), formError)
    }

    @Test
    fun eachWrongPassword_showsTheCountTheServerReturned() = runTest {
        val shown = (4 downTo 1).map { remaining ->
            loginAnswered(LoginResult.Failure(LoginError.INVALID_CREDENTIALS, remainingAttempts = remaining))
        }
        assertEquals((4 downTo 1).map { LoginFormError.InvalidCredentials(it) }, shown)
    }

    @Test
    fun lockedAccount_showsTheMinutesUntilTheLockEnds() = runTest {
        val formError = loginAnswered(LoginResult.Failure(LoginError.ACCOUNT_LOCKED, retryAfterMinutes = 15))
        assertEquals(LoginFormError.AccountLocked(15), formError)
    }

    @Test
    fun lockedAccountWithoutMinutes_showsTheGeneralLockMessage() = runTest {
        val formError = loginAnswered(LoginResult.Failure(LoginError.ACCOUNT_LOCKED))
        assertEquals(LoginFormError.Message(R.string.login_error_account_locked), formError)
    }

    @Test
    fun wrongPasswordWithoutCount_showsThePlainMessage() = runTest {
        val formError = loginAnswered(LoginResult.Failure(LoginError.INVALID_CREDENTIALS))
        assertEquals(LoginFormError.Message(R.string.login_error_invalid_credentials), formError)
    }

    @Test
    fun wrongPasswordAndLock_clearThePassword() = runTest {
        loginAnswered(LoginResult.Failure(LoginError.INVALID_CREDENTIALS, remainingAttempts = 1))
        assertEquals("", viewModel.password)
        loginAnswered(LoginResult.Failure(LoginError.ACCOUNT_LOCKED, retryAfterMinutes = 15))
        assertEquals("", viewModel.password)
    }

    @Test
    fun networkError_keepsThePasswordAndShowsItsMessage() = runTest {
        val formError = loginAnswered(LoginResult.Failure(LoginError.NETWORK))
        assertEquals(LoginFormError.Message(R.string.login_error_network), formError)
        assertEquals(PASSWORD, viewModel.password)
    }

    @Test
    fun typingAfterTheLockMessage_clearsIt() = runTest {
        loginAnswered(LoginResult.Failure(LoginError.ACCOUNT_LOCKED, retryAfterMinutes = 15))
        viewModel.onPasswordChange(PASSWORD)
        assertNull(viewModel.uiState.value.formError)
    }

    @Test
    fun login_sendsTheTrimmedEmail() = runTest {
        repository.loginResult = LoginResult.Success
        viewModel.onEmailChange(" $EMAIL ")
        viewModel.onPasswordChange(PASSWORD)
        viewModel.onLoginClick()
        advanceUntilIdle()
        assertEquals(listOf(EMAIL to PASSWORD), repository.loginRequests)
    }
}
