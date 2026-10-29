package com.example.paypulse.ui.auth.activation

import com.example.paypulse.MainDispatcherRule
import com.example.paypulse.R
import com.example.paypulse.domain.activation.ActivationError
import com.example.paypulse.domain.activation.ActivationResult
import com.example.paypulse.domain.activation.ActivationTokenStatus
import com.example.paypulse.domain.activation.TokenCheckResult
import com.example.paypulse.domain.auth.PasswordPolicy
import com.example.paypulse.domain.auth.PasswordRule
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

private const val TOKEN = "test-token"
private const val WEAK_PASSWORD = "lozinka"

@OptIn(ExperimentalCoroutinesApi::class)
class ActivationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = StubActivationRepository()
    private val validPassword = "Lozinka1".padEnd(PasswordPolicy.MIN_LENGTH, 'x')

    private fun TestScope.openedViewModel(): ActivationViewModel =
        ActivationViewModel(TOKEN, repository).also { advanceUntilIdle() }

    private fun ActivationViewModel.fillIn(password: String, confirmPassword: String = password) {
        onPasswordChange(password)
        onConfirmPasswordChange(confirmPassword)
    }

    private fun TestScope.pageAfterCheck(result: TokenCheckResult): ActivationPage {
        repository.tokenCheckResult = result
        return openedViewModel().uiState.value.page
    }

    private fun TestScope.submittedWithResult(result: ActivationResult): ActivationViewModel {
        repository.activationResult = result
        return openedViewModel().apply {
            fillIn(validPassword)
            onSubmitClick()
            advanceUntilIdle()
        }
    }

    @Test
    fun opening_checksTheTokenAndShowsLoadingFirst() = runTest {
        val viewModel = ActivationViewModel(TOKEN, repository)
        assertEquals(ActivationPage.CheckingLink, viewModel.uiState.value.page)
        advanceUntilIdle()
        assertEquals(listOf(TOKEN), repository.checkedTokens)
    }

    @Test
    fun validToken_showsForm() = runTest {
        assertEquals(ActivationPage.Form, pageAfterCheck(TokenCheckResult.Checked(ActivationTokenStatus.VALID)))
    }

    @Test
    fun expiredOrUsedToken_showsMatchingPage() = runTest {
        val expired = pageAfterCheck(TokenCheckResult.Checked(ActivationTokenStatus.EXPIRED))
        val used = pageAfterCheck(TokenCheckResult.Checked(ActivationTokenStatus.USED))
        assertEquals(ActivationPage.LinkProblem(R.string.activation_link_expired), expired)
        assertEquals(ActivationPage.LinkProblem(R.string.activation_link_used), used)
    }

    @Test
    fun anyOtherCheckError_showsInvalidPage() = runTest {
        val invalidPage = ActivationPage.LinkProblem(R.string.activation_link_invalid)
        assertEquals(invalidPage, pageAfterCheck(TokenCheckResult.Failure(ActivationError.TOKEN_INVALID)))
        assertEquals(invalidPage, pageAfterCheck(TokenCheckResult.Failure(ActivationError.UNKNOWN)))
    }

    @Test
    fun networkErrorOnCheck_showsRetryPage() = runTest {
        val page = pageAfterCheck(TokenCheckResult.Failure(ActivationError.NETWORK))
        assertEquals(ActivationPage.LinkProblem(R.string.activation_link_check_failed, canRetry = true), page)
    }

    @Test
    fun retry_checksTheLinkAgain() = runTest {
        repository.tokenCheckResult = TokenCheckResult.Failure(ActivationError.NETWORK)
        val viewModel = openedViewModel()
        repository.tokenCheckResult = TokenCheckResult.Checked(ActivationTokenStatus.VALID)
        viewModel.onRetryClick()
        advanceUntilIdle()
        assertEquals(ActivationPage.Form, viewModel.uiState.value.page)
        assertEquals(2, repository.checkedTokens.size)
    }

    @Test
    fun typingPassword_updatesMetRulesLive() = runTest {
        val viewModel = openedViewModel()
        viewModel.onPasswordChange("a")
        assertEquals(setOf(PasswordRule.LOWERCASE), viewModel.uiState.value.metRules)
        viewModel.onPasswordChange(validPassword)
        assertEquals(PasswordRule.entries.toSet(), viewModel.uiState.value.metRules)
    }

    @Test
    fun mismatch_isNotShownBeforeSubmit() = runTest {
        val viewModel = openedViewModel()
        viewModel.fillIn(validPassword, "different")
        assertNull(viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun submitWithMismatch_showsErrorAndSendsNothing() = runTest {
        val viewModel = openedViewModel()
        viewModel.fillIn(validPassword, "different")
        viewModel.onSubmitClick()
        advanceUntilIdle()
        assertEquals(R.string.activation_error_mismatch, viewModel.uiState.value.confirmPasswordError)
        assertTrue(repository.activationRequests.isEmpty())
    }

    @Test
    fun fixingConfirmationAfterSubmit_clearsMismatchLive() = runTest {
        val viewModel = openedViewModel()
        viewModel.fillIn(validPassword, "different")
        viewModel.onSubmitClick()
        viewModel.onConfirmPasswordChange(validPassword)
        assertNull(viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun submitWithUnmetRules_highlightsThemAndSendsNothing() = runTest {
        val viewModel = openedViewModel()
        viewModel.fillIn(WEAK_PASSWORD)
        viewModel.onSubmitClick()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.highlightUnmetRules)
        assertTrue(repository.activationRequests.isEmpty())
    }

    @Test
    fun successfulSubmit_sendsValuesClearsPasswordsAndSignalsActivated() = runTest {
        val viewModel = submittedWithResult(ActivationResult.Success)
        val expectedRequest = StubActivationRepository.ActivationRequest(TOKEN, validPassword, validPassword)
        assertEquals(listOf(expectedRequest), repository.activationRequests)
        assertEquals("", viewModel.password)
        assertEquals("", viewModel.confirmPassword)
        assertTrue(viewModel.uiState.value.isActivated)
    }

    @Test
    fun doubleSubmitWhileLoading_sendsOnce() = runTest {
        val viewModel = openedViewModel()
        viewModel.fillIn(validPassword)
        viewModel.onSubmitClick()
        assertTrue(viewModel.uiState.value.isLoading)
        viewModel.onSubmitClick()
        advanceUntilIdle()
        assertEquals(1, repository.activationRequests.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun tokenErrorOnSubmit_showsMatchingPageAndClearsPasswords() = runTest {
        val viewModel = submittedWithResult(ActivationResult.Failure(ActivationError.TOKEN_USED))
        assertEquals(ActivationPage.LinkProblem(R.string.activation_link_used), viewModel.uiState.value.page)
        assertEquals("", viewModel.password)
        assertFalse(viewModel.uiState.value.isActivated)
    }

    @Test
    fun serverPasswordPolicyError_showsBannerAndHighlightsRules() = runTest {
        val viewModel = submittedWithResult(ActivationResult.Failure(ActivationError.PASSWORD_POLICY))
        assertEquals(R.string.activation_error_password_policy, viewModel.uiState.value.formError)
        assertTrue(viewModel.uiState.value.highlightUnmetRules)
    }

    @Test
    fun serverMismatchError_showsErrorUnderConfirmField() = runTest {
        val viewModel = submittedWithResult(ActivationResult.Failure(ActivationError.PASSWORD_MISMATCH))
        assertEquals(R.string.activation_error_mismatch, viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun networkErrorOnSubmit_showsBannerAndKeepsPasswords() = runTest {
        val viewModel = submittedWithResult(ActivationResult.Failure(ActivationError.NETWORK))
        assertEquals(R.string.activation_error_network, viewModel.uiState.value.formError)
        assertEquals(validPassword, viewModel.password)
        assertEquals(ActivationPage.Form, viewModel.uiState.value.page)
    }

    @Test
    fun typingAfterFormError_clearsTheError() = runTest {
        val viewModel = submittedWithResult(ActivationResult.Failure(ActivationError.NETWORK))
        viewModel.onPasswordChange(validPassword + "x")
        assertNull(viewModel.uiState.value.formError)
    }
}
