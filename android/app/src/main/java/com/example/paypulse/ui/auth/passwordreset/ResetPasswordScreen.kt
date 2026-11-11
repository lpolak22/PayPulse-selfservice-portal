package com.example.paypulse.ui.auth.passwordreset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.ui.components.FullHeightScrollColumn
import com.example.paypulse.ui.components.LoadingButton
import com.example.paypulse.ui.components.LoadingScreen
import com.example.paypulse.ui.components.MessageBanner
import com.example.paypulse.ui.components.PasswordRuleChecklist
import com.example.paypulse.ui.components.PasswordTextField

class ResetPasswordActions(
    val onPasswordChange: (String) -> Unit,
    val onConfirmPasswordChange: (String) -> Unit,
    val onPasswordVisibilityToggle: () -> Unit,
    val onConfirmPasswordVisibilityToggle: () -> Unit,
    val onSubmitClick: () -> Unit,
    val onRetryClick: () -> Unit,
    val onRequestNewLinkClick: () -> Unit,
    val onBackToLoginClick: () -> Unit
)

@Composable
fun ResetPasswordScreen(
    password: String,
    confirmPassword: String,
    state: ResetPasswordUiState,
    actions: ResetPasswordActions,
    modifier: Modifier = Modifier
) {
    when (val page = state.page) {
        ResetPasswordPage.CheckingLink -> LoadingScreen(modifier = modifier)
        ResetPasswordPage.Form -> ResetPasswordForm(password, confirmPassword, state, actions, modifier)
        is ResetPasswordPage.LinkProblem -> ResetPasswordLinkProblemScreen(
            problem = page,
            onRetryClick = actions.onRetryClick,
            onRequestNewLinkClick = actions.onRequestNewLinkClick,
            onBackToLoginClick = actions.onBackToLoginClick,
            modifier = modifier
        )
    }
}

@Composable
private fun ResetPasswordForm(
    password: String,
    confirmPassword: String,
    state: ResetPasswordUiState,
    actions: ResetPasswordActions,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            FullHeightScrollColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                modifier = Modifier.weight(1f)
            ) {
                ResetPasswordHeader()
                state.formError?.let { MessageBanner(message = stringResource(it)) }
                PasswordTextField(
                    value = password,
                    onValueChange = actions.onPasswordChange,
                    label = stringResource(R.string.reset_password_password_label),
                    isPasswordVisible = state.isPasswordVisible,
                    onVisibilityToggle = actions.onPasswordVisibilityToggle,
                    modifier = Modifier.fillMaxWidth(),
                    imeAction = ImeAction.Next
                )
                PasswordTextField(
                    value = confirmPassword,
                    onValueChange = actions.onConfirmPasswordChange,
                    label = stringResource(R.string.reset_password_confirm_password_label),
                    isPasswordVisible = state.isConfirmPasswordVisible,
                    onVisibilityToggle = actions.onConfirmPasswordVisibilityToggle,
                    modifier = Modifier.fillMaxWidth(),
                    errorMessage = state.confirmPasswordError?.let { stringResource(it) },
                    keyboardActions = KeyboardActions(onDone = { actions.onSubmitClick() })
                )
                PasswordRules(state)
            }
            LoadingButton(
                text = stringResource(R.string.reset_password_submit),
                loadingDescription = stringResource(R.string.reset_password_loading),
                isLoading = state.isLoading,
                onClick = actions.onSubmitClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun ResetPasswordHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.reset_password_title),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.reset_password_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PasswordRules(state: ResetPasswordUiState) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.reset_password_rules_title), style = MaterialTheme.typography.titleSmall)
        PasswordRuleChecklist(metRules = state.metRules, highlightUnmetRules = state.highlightUnmetRules)
    }
}
