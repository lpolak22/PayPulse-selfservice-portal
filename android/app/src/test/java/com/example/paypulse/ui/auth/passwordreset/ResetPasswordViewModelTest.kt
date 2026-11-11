package com.example.paypulse.ui.auth.passwordreset

import com.example.paypulse.MainDispatcherRule
import com.example.paypulse.R
import com.example.paypulse.domain.auth.PasswordRule
import com.example.paypulse.domain.passwordreset.PasswordResetError
import com.example.paypulse.domain.passwordreset.PasswordResetResult
import com.example.paypulse.domain.passwordreset.ResetTokenCheckResult
import com.example.paypulse.domain.passwordreset.ResetTokenStatus
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

private const val TOKEN = "reset-token"
private const val VALID_PASSWORD = "Novalozinka1"
private const val WEAK_PASSWORD = "lozinka"

@OptIn(ExperimentalCoroutinesApi::class)
class ResetPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = StubPasswordResetRepository()

    private fun TestScope.openedViewModel(): ResetPasswordViewModel =
        ResetPasswordViewModel(TOKEN, repository).also { advanceUntilIdle() }

    private fun ResetPasswordViewModel.fillIn(password: String, confirmPassword: String = password) {
        onPasswordChange(password)
        onConfirmPasswordChange(confirmPassword)
    }

    private fun TestScope.pageAfterCheck(result: ResetTokenCheckResult): ResetPasswordPage {
        repository.tokenCheckResult = result
        return openedViewModel().uiState.value.page
    }

    private fun TestScope.submittedWithResult(result: PasswordResetResult): ResetPasswordViewModel {
        repository.resetResult = result
        return openedViewModel().apply {
            fillIn(VALID_PASSWORD)
            onSubmitClick()
            advanceUntilIdle()
        }
    }

    @Test
    fun opening_checksTheTokenAndShowsLoadingFirst() = runTest {
        val viewModel = ResetPasswordViewModel(TOKEN, repository)
        assertEquals(ResetPasswordPage.CheckingLink, viewModel.uiState.value.page)
        advanceUntilIdle()
        assertEquals(listOf(TOKEN), repository.checkedTokens)
        assertEquals(ResetPasswordPage.Form, viewModel.uiState.value.page)
    }

    @Test
    fun expiredOrUsedToken_showsMatchingPage() = runTest {
        val expired = pageAfterCheck(ResetTokenCheckResult.Checked(ResetTokenStatus.EXPIRED))
        val used = pageAfterCheck(ResetTokenCheckResult.Checked(ResetTokenStatus.USED))
        assertEquals(ResetPasswordPage.LinkProblem(R.string.reset_password_link_expired), expired)
        assertEquals(ResetPasswordPage.LinkProblem(R.string.reset_password_link_used), used)
    }

    @Test
    fun invalidOrUnknownCheckError_showsInvalidPage() = runTest {
        val invalidPage = ResetPasswordPage.LinkProblem(R.string.reset_password_link_invalid)
        assertEquals(invalidPage, pageAfterCheck(ResetTokenCheckResult.Failure(PasswordResetError.TOKEN_INVALID)))
        assertEquals(invalidPage, pageAfterCheck(ResetTokenCheckResult.Failure(PasswordResetError.UNKNOWN)))
    }

    @Test
    fun networkErrorOnCheck_showsRetryPageAndRetryWorks() = runTest {
        repository.tokenCheckResult = ResetTokenCheckResult.Failure(PasswordResetError.NETWORK)
        val viewModel = openedViewModel()
        val retryPage = ResetPasswordPage.LinkProblem(R.string.reset_password_link_check_failed, canRetry = true)
        assertEquals(retryPage, viewModel.uiState.value.page)
        repository.tokenCheckResult = ResetTokenCheckResult.Checked(ResetTokenStatus.VALID)
        viewModel.onRetryClick()
        advanceUntilIdle()
        assertEquals(ResetPasswordPage.Form, viewModel.uiState.value.page)
        assertEquals(2, repository.checkedTokens.size)
    }

    @Test
    fun typingPassword_updatesMetRulesLive() = runTest {
        val viewModel = openedViewModel()
        viewModel.onPasswordChange(VALID_PASSWORD)
        assertEquals(PasswordRule.entries.toSet(), viewModel.uiState.value.metRules)
    }

    @Test
    fun submitWithMismatch_showsErrorAndSendsNothing() = runTest {
        val viewModel = openedViewModel()
        viewModel.fillIn(VALID_PASSWORD, "different")
        viewModel.onSubmitClick()
        advanceUntilIdle()
        assertEquals(R.string.reset_password_error_mismatch, viewModel.uiState.value.confirmPasswordError)
        assertTrue(repository.resetRequests.isEmpty())
    }

    @Test
    fun submitWithUnmetRules_highlightsThemAndSendsNothing() = runTest {
        val viewModel = openedViewModel()
        viewModel.fillIn(WEAK_PASSWORD)
        viewModel.onSubmitClick()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.highlightUnmetRules)
        assertTrue(repository.resetRequests.isEmpty())
    }

    @Test
    fun successfulSubmit_sendsValuesClearsPasswordsAndSignalsReset() = runTest {
        val viewModel = submittedWithResult(PasswordResetResult.Success)
        val expected = StubPasswordResetRepository.ResetRequest(TOKEN, VALID_PASSWORD, VALID_PASSWORD)
        assertEquals(listOf(expected), repository.resetRequests)
        assertEquals("", viewModel.password)
        assertEquals("", viewModel.confirmPassword)
        assertTrue(viewModel.uiState.value.isReset)
    }

    @Test
    fun doubleSubmitWhileLoading_sendsOnce() = runTest {
        val viewModel = openedViewModel()
        viewModel.fillIn(VALID_PASSWORD)
        viewModel.onSubmitClick()
        viewModel.onSubmitClick()
        advanceUntilIdle()
        assertEquals(1, repository.resetRequests.size)
    }

    @Test
    fun tokenErrorOnSubmit_showsMatchingPageAndClearsPasswords() = runTest {
        val viewModel = submittedWithResult(PasswordResetResult.Failure(PasswordResetError.TOKEN_EXPIRED))
        assertEquals(ResetPasswordPage.LinkProblem(R.string.reset_password_link_expired), viewModel.uiState.value.page)
        assertEquals("", viewModel.password)
        assertFalse(viewModel.uiState.value.isReset)
    }

    @Test
    fun serverPolicyAndMismatchErrors_areShownOnForm() = runTest {
        val policy = submittedWithResult(PasswordResetResult.Failure(PasswordResetError.PASSWORD_POLICY))
        assertEquals(R.string.reset_password_error_password_policy, policy.uiState.value.formError)
        val mismatch = submittedWithResult(PasswordResetResult.Failure(PasswordResetError.PASSWORD_MISMATCH))
        assertEquals(R.string.reset_password_error_mismatch, mismatch.uiState.value.confirmPasswordError)
    }

    @Test
    fun networkErrorOnSubmit_showsBannerAndKeepsPasswords() = runTest {
        val viewModel = submittedWithResult(PasswordResetResult.Failure(PasswordResetError.NETWORK))
        assertEquals(R.string.reset_password_error_network, viewModel.uiState.value.formError)
        assertEquals(VALID_PASSWORD, viewModel.password)
        assertEquals(ResetPasswordPage.Form, viewModel.uiState.value.page)
    }

    @Test
    fun leaving_clearsBothPasswords() = runTest {
        val viewModel = openedViewModel()
        viewModel.fillIn(VALID_PASSWORD)
        viewModel.onLeave()
        assertEquals("", viewModel.password)
        assertNull(viewModel.uiState.value.formError)
    }
}
