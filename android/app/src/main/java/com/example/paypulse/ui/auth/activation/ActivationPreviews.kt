package com.example.paypulse.ui.auth.activation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.paypulse.R
import com.example.paypulse.domain.auth.PasswordPolicy
import com.example.paypulse.ui.theme.PayPulseTheme

private const val VALID_PASSWORD = "Lozinka1"
private const val WEAK_PASSWORD = "loz"

private val formState = ActivationUiState(page = ActivationPage.Form)

private val submittedWithErrorsState = formState.copy(
    metRules = PasswordPolicy.metRules(WEAK_PASSWORD),
    highlightUnmetRules = true,
    confirmPasswordError = R.string.activation_error_mismatch
)

@PreviewLightDark
@Composable
private fun ActivationCheckingLinkPreview() = ActivationPreview(state = ActivationUiState())

@PreviewLightDark
@Composable
private fun ActivationEmptyFormPreview() = ActivationPreview(state = formState)

@PreviewLightDark
@Composable
private fun ActivationPartlyMetPreview() = ActivationPreview(
    password = WEAK_PASSWORD,
    state = formState.copy(metRules = PasswordPolicy.metRules(WEAK_PASSWORD))
)

@PreviewLightDark
@Composable
private fun ActivationAllMetPreview() = ActivationPreview(
    password = VALID_PASSWORD,
    confirmPassword = VALID_PASSWORD,
    state = formState.copy(metRules = PasswordPolicy.metRules(VALID_PASSWORD))
)

@PreviewLightDark
@Composable
private fun ActivationSubmittedWithErrorsPreview() = ActivationPreview(
    password = WEAK_PASSWORD,
    confirmPassword = "lo",
    state = submittedWithErrorsState
)

@PreviewLightDark
@Composable
private fun ActivationSavingPreview() = ActivationPreview(
    password = VALID_PASSWORD,
    confirmPassword = VALID_PASSWORD,
    state = formState.copy(metRules = PasswordPolicy.metRules(VALID_PASSWORD), isLoading = true)
)

@PreviewLightDark
@Composable
private fun ActivationNetworkErrorPreview() = ActivationPreview(
    password = VALID_PASSWORD,
    confirmPassword = VALID_PASSWORD,
    state = formState.copy(metRules = PasswordPolicy.metRules(VALID_PASSWORD), formError = R.string.activation_error_network)
)

@PreviewLightDark
@Composable
private fun ActivationLinkExpiredPreview() = ActivationPreview(
    state = ActivationUiState(page = ActivationPage.LinkProblem(R.string.activation_link_expired))
)

@PreviewLightDark
@Composable
private fun ActivationLinkUsedPreview() = ActivationPreview(
    state = ActivationUiState(page = ActivationPage.LinkProblem(R.string.activation_link_used))
)

@PreviewLightDark
@Composable
private fun ActivationLinkInvalidPreview() = ActivationPreview(
    state = ActivationUiState(page = ActivationPage.LinkProblem(R.string.activation_link_invalid))
)

@PreviewLightDark
@Composable
private fun ActivationLinkCheckFailedPreview() = ActivationPreview(
    state = ActivationUiState(page = ActivationPage.LinkProblem(R.string.activation_link_check_failed, canRetry = true))
)

@Preview(name = "Large font", fontScale = 2f, device = "spec:width=411dp,height=891dp")
@Composable
private fun ActivationLargeFontPreview() = ActivationPreview(
    password = WEAK_PASSWORD,
    confirmPassword = "lo",
    state = submittedWithErrorsState
)

@Composable
private fun ActivationPreview(
    password: String = "",
    confirmPassword: String = "",
    state: ActivationUiState
) {
    PayPulseTheme {
        ActivationScreen(
            password = password,
            confirmPassword = confirmPassword,
            state = state,
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onPasswordVisibilityToggle = {},
            onConfirmPasswordVisibilityToggle = {},
            onSubmitClick = {},
            onRetryClick = {},
            onBackToLoginClick = {}
        )
    }
}
