package com.example.paypulse.ui.auth.login

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.paypulse.R
import com.example.paypulse.ui.theme.PayPulseTheme

private const val SAMPLE_EMAIL = "korisnik@primjer.hr"
private const val SAMPLE_PASSWORD = "Lozinka123"

@PreviewLightDark
@Composable
private fun LoginEmptyPreview() = LoginPreview()

@PreviewLightDark
@Composable
private fun LoginFilledPreview() = LoginPreview(email = SAMPLE_EMAIL, password = SAMPLE_PASSWORD)

@PreviewLightDark
@Composable
private fun LoginPasswordVisiblePreview() = LoginPreview(
    email = SAMPLE_EMAIL,
    password = SAMPLE_PASSWORD,
    state = LoginUiState(isPasswordVisible = true)
)

@PreviewLightDark
@Composable
private fun LoginFieldErrorsPreview() = LoginPreview(
    email = "korisnik@",
    state = LoginUiState(
        emailError = R.string.login_error_email_invalid,
        passwordError = R.string.login_error_password_required
    )
)

@PreviewLightDark
@Composable
private fun LoginInvalidCredentialsPreview() = LoginPreview(
    email = SAMPLE_EMAIL,
    password = SAMPLE_PASSWORD,
    state = LoginUiState(formError = R.string.login_error_invalid_credentials)
)

@PreviewLightDark
@Composable
private fun LoginSessionExpiredPreview() = LoginPreview(
    email = SAMPLE_EMAIL,
    state = LoginUiState(formError = R.string.login_error_session_expired)
)

@PreviewLightDark
@Composable
private fun LoginActivationNoticePreview() = LoginPreview(notice = R.string.activation_success_notice)

@PreviewLightDark
@Composable
private fun LoginLoadingPreview() = LoginPreview(
    email = SAMPLE_EMAIL,
    password = SAMPLE_PASSWORD,
    state = LoginUiState(isLoading = true)
)

@Preview(name = "Small phone", device = "spec:width=320dp,height=568dp")
@Composable
private fun LoginSmallPhonePreview() = LoginPreview(
    email = "korisnik@",
    state = LoginUiState(
        emailError = R.string.login_error_email_invalid,
        passwordError = R.string.login_error_password_required,
        formError = R.string.login_error_invalid_credentials
    )
)

@Preview(name = "Large font", fontScale = 2f, device = "spec:width=411dp,height=891dp")
@Composable
private fun LoginLargeFontPreview() = LoginPreview(
    email = SAMPLE_EMAIL,
    password = SAMPLE_PASSWORD,
    state = LoginUiState(formError = R.string.login_error_invalid_credentials)
)

@Composable
private fun LoginPreview(
    email: String = "",
    password: String = "",
    state: LoginUiState = LoginUiState(),
    @StringRes notice: Int? = null
) {
    PayPulseTheme {
        LoginScreen(
            email = email,
            password = password,
            state = state,
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordVisibilityToggle = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            notice = notice
        )
    }
}
