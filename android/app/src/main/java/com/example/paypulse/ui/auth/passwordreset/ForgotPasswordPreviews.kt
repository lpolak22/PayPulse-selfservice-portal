package com.example.paypulse.ui.auth.passwordreset

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.paypulse.R
import com.example.paypulse.ui.theme.PayPulseTheme

@PreviewLightDark
@Composable
private fun ForgotPasswordEmptyPreview() = ForgotPasswordPreview(state = ForgotPasswordUiState())

@PreviewLightDark
@Composable
private fun ForgotPasswordInvalidEmailPreview() = ForgotPasswordPreview(
    email = "ana@",
    state = ForgotPasswordUiState(emailError = R.string.forgot_password_error_email_invalid)
)

@PreviewLightDark
@Composable
private fun ForgotPasswordSendingPreview() = ForgotPasswordPreview(
    email = "ana@example.com",
    state = ForgotPasswordUiState(isLoading = true)
)

@PreviewLightDark
@Composable
private fun ForgotPasswordRateLimitedPreview() = ForgotPasswordPreview(
    email = "ana@example.com",
    state = ForgotPasswordUiState(formError = R.string.forgot_password_error_rate_limited)
)

@PreviewLightDark
@Composable
private fun ForgotPasswordSentPreview() = ForgotPasswordPreview(state = ForgotPasswordUiState(isSent = true))

@Preview(name = "Large font", fontScale = 2f, device = "spec:width=411dp,height=891dp")
@Composable
private fun ForgotPasswordLargeFontPreview() = ForgotPasswordPreview(
    email = "ana@",
    state = ForgotPasswordUiState(emailError = R.string.forgot_password_error_email_invalid)
)

@Composable
private fun ForgotPasswordPreview(email: String = "", state: ForgotPasswordUiState) {
    PayPulseTheme {
        ForgotPasswordScreen(
            email = email,
            state = state,
            onEmailChange = {},
            onSubmitClick = {},
            onBackToLoginClick = {}
        )
    }
}
