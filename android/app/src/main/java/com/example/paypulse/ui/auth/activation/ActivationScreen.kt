package com.example.paypulse.ui.auth.activation

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

@Composable
fun ActivationScreen(
    password: String,
    confirmPassword: String,
    state: ActivationUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    onSubmitClick: () -> Unit,
    onRetryClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (val page = state.page) {
        ActivationPage.CheckingLink -> LoadingScreen(modifier = modifier)
        ActivationPage.Form -> ActivationForm(
            password = password,
            confirmPassword = confirmPassword,
            state = state,
            onPasswordChange = onPasswordChange,
            onConfirmPasswordChange = onConfirmPasswordChange,
            onPasswordVisibilityToggle = onPasswordVisibilityToggle,
            onConfirmPasswordVisibilityToggle = onConfirmPasswordVisibilityToggle,
            onSubmitClick = onSubmitClick,
            modifier = modifier
        )
        is ActivationPage.LinkProblem -> ActivationLinkProblemScreen(
            problem = page,
            onRetryClick = onRetryClick,
            onBackToLoginClick = onBackToLoginClick,
            modifier = modifier
        )
    }
}

@Composable
private fun ActivationForm(
    password: String,
    confirmPassword: String,
    state: ActivationUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    onSubmitClick: () -> Unit,
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
                ActivationHeader()
                state.formError?.let { MessageBanner(message = stringResource(it)) }
                PasswordField(password, state.isPasswordVisible, onPasswordChange, onPasswordVisibilityToggle)
                ConfirmPasswordField(
                    confirmPassword = confirmPassword,
                    state = state,
                    onValueChange = onConfirmPasswordChange,
                    onVisibilityToggle = onConfirmPasswordVisibilityToggle,
                    onDone = onSubmitClick
                )
                PasswordRules(state)
            }
            SubmitButton(isLoading = state.isLoading, onClick = onSubmitClick)
        }
    }
}

@Composable
private fun ActivationHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.activation_title),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.activation_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PasswordField(
    password: String,
    isVisible: Boolean,
    onValueChange: (String) -> Unit,
    onVisibilityToggle: () -> Unit
) {
    PasswordTextField(
        value = password,
        onValueChange = onValueChange,
        label = stringResource(R.string.activation_password_label),
        isPasswordVisible = isVisible,
        onVisibilityToggle = onVisibilityToggle,
        modifier = Modifier.fillMaxWidth(),
        imeAction = ImeAction.Next
    )
}

@Composable
private fun ConfirmPasswordField(
    confirmPassword: String,
    state: ActivationUiState,
    onValueChange: (String) -> Unit,
    onVisibilityToggle: () -> Unit,
    onDone: () -> Unit
) {
    PasswordTextField(
        value = confirmPassword,
        onValueChange = onValueChange,
        label = stringResource(R.string.activation_confirm_password_label),
        isPasswordVisible = state.isConfirmPasswordVisible,
        onVisibilityToggle = onVisibilityToggle,
        modifier = Modifier.fillMaxWidth(),
        errorMessage = state.confirmPasswordError?.let { stringResource(it) },
        keyboardActions = KeyboardActions(onDone = { onDone() })
    )
}

@Composable
private fun PasswordRules(state: ActivationUiState) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.activation_rules_title), style = MaterialTheme.typography.titleSmall)
        PasswordRuleChecklist(metRules = state.metRules, highlightUnmetRules = state.highlightUnmetRules)
    }
}

@Composable
private fun SubmitButton(isLoading: Boolean, onClick: () -> Unit) {
    LoadingButton(
        text = stringResource(R.string.activation_submit),
        loadingDescription = stringResource(R.string.activation_loading),
        isLoading = isLoading,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    )
}
