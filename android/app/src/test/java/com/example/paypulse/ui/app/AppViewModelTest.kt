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
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

private const val ACTIVATION_LINK = "paypulse://activate?token=abc"

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = StubAuthRepository()

    private val loggedInUser = User(
        id = 1,
        firstName = "Ana",
        lastName = "Horvat",
        email = "ana.horvat@primjer.hr",
        phone = null,
        role = UserRole.USER,
        status = UserStatus.ACTIVE,
        createdAt = LocalDateTime.of(2026, 10, 1, 9, 0)
    )

    private fun TestScope.startedViewModel(): AppViewModel =
        AppViewModel(authRepository).also { advanceUntilIdle() }

    @Test
    fun validLink_opensActivationWithItsToken() = runTest {
        val viewModel = startedViewModel()
        viewModel.onActivationLink(ACTIVATION_LINK)
        assertEquals("abc", viewModel.activationRequest.value?.token)
    }

    @Test
    fun invalidOrMissingLink_isIgnored() = runTest {
        val viewModel = startedViewModel()
        viewModel.onActivationLink("https://example.com")
        viewModel.onActivationLink(null)
        assertNull(viewModel.activationRequest.value)
    }

    @Test
    fun openingTheSameLinkTwice_createsADifferentRequest() = runTest {
        val viewModel = startedViewModel()
        viewModel.onActivationLink(ACTIVATION_LINK)
        val firstId = viewModel.activationRequest.value?.id
        viewModel.onActivationLink(ACTIVATION_LINK)
        assertNotEquals(firstId, viewModel.activationRequest.value?.id)
    }

    @Test
    fun success_closesActivationAndShowsNotice() = runTest {
        val viewModel = startedViewModel()
        viewModel.onActivationLink(ACTIVATION_LINK)
        viewModel.onActivationSucceeded()
        assertNull(viewModel.activationRequest.value)
        assertEquals(R.string.activation_success_notice, viewModel.loginNotice.value)
    }

    @Test
    fun closing_closesActivationWithoutNotice() = runTest {
        val viewModel = startedViewModel()
        viewModel.onActivationLink(ACTIVATION_LINK)
        viewModel.onActivationClosed()
        assertNull(viewModel.activationRequest.value)
        assertNull(viewModel.loginNotice.value)
    }

    @Test
    fun successWhileLoggedIn_showsNoNotice() = runTest {
        authRepository.session.value = SessionState.LoggedIn(loggedInUser)
        val viewModel = startedViewModel()
        viewModel.onActivationLink(ACTIVATION_LINK)
        viewModel.onActivationSucceeded()
        assertNull(viewModel.loginNotice.value)
    }

    @Test
    fun loggingIn_clearsNotice() = runTest {
        val viewModel = startedViewModel()
        viewModel.onActivationLink(ACTIVATION_LINK)
        viewModel.onActivationSucceeded()
        authRepository.session.value = SessionState.LoggedIn(loggedInUser)
        advanceUntilIdle()
        assertNull(viewModel.loginNotice.value)
    }

    @Test
    fun newLink_clearsOldNotice() = runTest {
        val viewModel = startedViewModel()
        viewModel.onActivationLink(ACTIVATION_LINK)
        viewModel.onActivationSucceeded()
        viewModel.onActivationLink(ACTIVATION_LINK)
        assertNull(viewModel.loginNotice.value)
    }
}
