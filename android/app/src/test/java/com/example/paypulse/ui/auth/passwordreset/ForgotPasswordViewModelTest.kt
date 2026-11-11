package com.example.paypulse.ui.auth.passwordreset

import com.example.paypulse.MainDispatcherRule
import com.example.paypulse.R
import com.example.paypulse.domain.passwordreset.PasswordResetError
import com.example.paypulse.domain.passwordreset.PasswordResetResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val EMAIL = "ana@example.com"

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = StubPasswordResetRepository()
    private val viewModel = ForgotPasswordViewModel(repository)

    private fun TestScope.submitted(email: String, result: PasswordResetResult = PasswordResetResult.Success) {
        repository.requestResult = result
        viewModel.onEmailChange(email)
        viewModel.onSubmitClick()
        advanceUntilIdle()
    }

    @Test
    fun emptyEmail_showsRequiredErrorAndSendsNothing() = runTest {
        submitted("   ")
        assertEquals(R.string.forgot_password_error_email_required, viewModel.uiState.value.emailError)
        assertTrue(repository.requestedEmails.isEmpty())
    }

    @Test
    fun invalidEmail_showsInvalidErrorAndSendsNothing() = runTest {
        submitted("ana@")
        assertEquals(R.string.forgot_password_error_email_invalid, viewModel.uiState.value.emailError)
        assertTrue(repository.requestedEmails.isEmpty())
    }

    @Test
    fun validEmail_isSentTrimmedAndShowsSentPage() = runTest {
        submitted("  $EMAIL ")
        assertEquals(listOf(EMAIL), repository.requestedEmails)
        assertTrue(viewModel.uiState.value.isSent)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun rateLimited_showsBannerAndStaysOnForm() = runTest {
        submitted(EMAIL, PasswordResetResult.Failure(PasswordResetError.RATE_LIMITED))
        assertEquals(R.string.forgot_password_error_rate_limited, viewModel.uiState.value.formError)
        assertFalse(viewModel.uiState.value.isSent)
    }

    @Test
    fun networkAndUnknownErrors_showMatchingBanner() = runTest {
        submitted(EMAIL, PasswordResetResult.Failure(PasswordResetError.NETWORK))
        assertEquals(R.string.forgot_password_error_network, viewModel.uiState.value.formError)
        submitted(EMAIL, PasswordResetResult.Failure(PasswordResetError.UNKNOWN))
        assertEquals(R.string.forgot_password_error_unknown, viewModel.uiState.value.formError)
    }

    @Test
    fun serverEmailValidationError_showsErrorUnderField() = runTest {
        submitted(EMAIL, PasswordResetResult.Failure(PasswordResetError.INVALID_EMAIL))
        assertEquals(R.string.forgot_password_error_email_invalid, viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.formError)
    }

    @Test
    fun typing_clearsErrors() = runTest {
        submitted(EMAIL, PasswordResetResult.Failure(PasswordResetError.NETWORK))
        viewModel.onEmailChange("ana@example.co")
        assertNull(viewModel.uiState.value.formError)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun doubleSubmitWhileLoading_sendsOnce() = runTest {
        viewModel.onEmailChange(EMAIL)
        viewModel.onSubmitClick()
        assertTrue(viewModel.uiState.value.isLoading)
        viewModel.onSubmitClick()
        advanceUntilIdle()
        assertEquals(1, repository.requestedEmails.size)
    }
}
