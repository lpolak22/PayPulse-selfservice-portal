package com.example.paypulse.ui.app

import com.example.paypulse.MainDispatcherRule
import com.example.paypulse.R
import com.example.paypulse.domain.auth.SessionState
import com.example.paypulse.domain.user.User
import com.example.paypulse.domain.user.UserRole
import com.example.paypulse.domain.user.UserStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

private const val RESET_LINK = "paypulse://reset-password?token=xyz"
private const val ACTIVATION_LINK = "paypulse://activate?token=abc"

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelPasswordResetTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = StubAuthRepository()

    private fun TestScope.startedViewModel(): AppViewModel =
        AppViewModel(authRepository).also { advanceUntilIdle() }

    @Test
    fun forgotPasswordClick_opensAndCloseClosesIt() = runTest {
        val viewModel = startedViewModel()
        viewModel.onForgotPasswordClick()
        assertNotNull(viewModel.forgotPasswordId.value)
        viewModel.onForgotPasswordClosed()
        assertNull(viewModel.forgotPasswordId.value)
    }

    @Test
    fun openingForgotPasswordTwice_givesADifferentScreen() = runTest {
        val viewModel = startedViewModel()
        viewModel.onForgotPasswordClick()
        val firstId = viewModel.forgotPasswordId.value
        viewModel.onForgotPasswordClosed()
        viewModel.onForgotPasswordClick()
        assertNotEquals(firstId, viewModel.forgotPasswordId.value)
    }

    @Test
    fun resetLink_opensResetWithItsTokenAndClosesForgotPassword() = runTest {
        val viewModel = startedViewModel()
        viewModel.onForgotPasswordClick()
        viewModel.onAppLink(RESET_LINK)
        assertEquals("xyz", viewModel.passwordResetRequest.value?.token)
        assertNull(viewModel.forgotPasswordId.value)
        assertNull(viewModel.activationRequest.value)
    }

    @Test
    fun appLink_routesActivationAndResetLinksToTheRightScreen() = runTest {
        val viewModel = startedViewModel()
        viewModel.onAppLink(ACTIVATION_LINK)
        assertEquals("abc", viewModel.activationRequest.value?.token)
        assertNull(viewModel.passwordResetRequest.value)
    }

    @Test
    fun unknownLink_opensNothing() = runTest {
        val viewModel = startedViewModel()
        viewModel.onAppLink("https://example.com")
        viewModel.onAppLink(null)
        assertNull(viewModel.passwordResetRequest.value)
        assertNull(viewModel.activationRequest.value)
    }

    @Test
    fun resetSuccess_closesResetAndShowsNotice() = runTest {
        val viewModel = startedViewModel()
        viewModel.onAppLink(RESET_LINK)
        viewModel.onPasswordResetSucceeded()
        advanceUntilIdle()
        assertNull(viewModel.passwordResetRequest.value)
        assertEquals(R.string.reset_password_success_notice, viewModel.loginNotice.value)
        assertEquals(0, authRepository.logoutCalls)
    }

    @Test
    fun resetSuccessWhileLoggedIn_logsOut() = runTest {
        authRepository.session.value = SessionState.LoggedIn(
            User(
                id = 1,
                firstName = "Ana",
                lastName = "Horvat",
                email = "ana.horvat@primjer.hr",
                phone = null,
                role = UserRole.USER,
                status = UserStatus.ACTIVE,
                createdAt = LocalDateTime.of(2026, 10, 1, 9, 0)
            )
        )
        val viewModel = startedViewModel()
        viewModel.onAppLink(RESET_LINK)
        viewModel.onPasswordResetSucceeded()
        advanceUntilIdle()
        assertEquals(1, authRepository.logoutCalls)
    }

    @Test
    fun closingReset_showsNoNotice() = runTest {
        val viewModel = startedViewModel()
        viewModel.onAppLink(RESET_LINK)
        viewModel.onPasswordResetClosed()
        assertNull(viewModel.passwordResetRequest.value)
        assertNull(viewModel.loginNotice.value)
    }

    @Test
    fun requestingNewLink_closesResetAndOpensForgotPassword() = runTest {
        val viewModel = startedViewModel()
        viewModel.onAppLink(RESET_LINK)
        viewModel.onNewResetLinkRequested()
        assertNull(viewModel.passwordResetRequest.value)
        assertNotNull(viewModel.forgotPasswordId.value)
    }
}
