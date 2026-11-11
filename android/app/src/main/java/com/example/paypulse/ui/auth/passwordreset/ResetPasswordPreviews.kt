package com.example.paypulse.ui.auth.passwordreset

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.paypulse.R
import com.example.paypulse.domain.auth.PasswordPolicy
import com.example.paypulse.ui.theme.PayPulseTheme

private const val VALID_PASSWORD = "Lozinka1"
private const val WEAK_PASSWORD = "loz"

private val formState = ResetPasswordUiState(page = ResetPasswordPage.Form)

private val submittedWithErrorsState = formState.copy(
    metRules = PasswordPolicy.metRules(WEAK_PASSWORD),
    highlightUnmetRules = true,
    confirmPasswordError = R.string.reset_password_error_mismatch
)

@PreviewLightDark
@Composable
private fun ResetPasswordCheckingLinkPreview() = ResetPasswordPreview(state = ResetPasswordUiState())

@PreviewLightDark
@Composable
private fun ResetPasswordEmptyFormPreview() = ResetPasswordPreview(state = formState)

@PreviewLightDark
@Composable
private fun ResetPasswordSubmittedWithErrorsPreview() = ResetPasswordPreview(
    password = WEAK_PASSWORD,
    confirmPassword = "lo",
    state = submittedWithErrorsState
)

@PreviewLightDark
@Composable
private fun ResetPasswordSavingPreview() = ResetPasswordPreview(
    password = VALID_PASSWORD,
    confirmPassword = VALID_PASSWORD,
    state = formState.copy(metRules = PasswordPolicy.metRules(VALID_PASSWORD), isLoading = true)
)

@PreviewLightDark
@Composable
private fun ResetPasswordLinkExpiredPreview() = ResetPasswordPreview(
    state = ResetPasswordUiState(page = ResetPasswordPage.LinkProblem(R.string.reset_password_link_expired))
)

@PreviewLightDark
@Composable
private fun ResetPasswordLinkCheckFailedPreview() = ResetPasswordPreview(
    state = ResetPasswordUiState(
        page = ResetPasswordPage.LinkProblem(R.string.reset_password_link_check_failed, canRetry = true)
    )
)

@Preview(name = "Large font", fontScale = 2f, device = "spec:width=411dp,height=891dp")
@Composable
private fun ResetPasswordLargeFontPreview() = ResetPasswordPreview(
    password = WEAK_PASSWORD,
    confirmPassword = "lo",
    state = submittedWithErrorsState
)

@Composable
private fun ResetPasswordPreview(
    password: String = "",
    confirmPassword: String = "",
    state: ResetPasswordUiState
) {
    PayPulseTheme {
        ResetPasswordScreen(
            password = password,
            confirmPassword = confirmPassword,
            state = state,
            actions = ResetPasswordActions(
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onPasswordVisibilityToggle = {},
                onConfirmPasswordVisibilityToggle = {},
                onSubmitClick = {},
                onRetryClick = {},
                onRequestNewLinkClick = {},
                onBackToLoginClick = {}
            )
        )
    }
}
