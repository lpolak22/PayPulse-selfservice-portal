package com.example.paypulse.ui.auth.login

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.ui.components.FullHeightScrollColumn
import com.example.paypulse.ui.components.LoadingButton
import com.example.paypulse.ui.components.MessageBanner
import com.example.paypulse.ui.components.MessageBannerStyle
import com.example.paypulse.ui.components.PasswordTextField

@Composable
fun LoginScreen(
    email: String,
    password: String,
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes notice: Int? = null
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            FullHeightScrollColumn(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.weight(1f)) {
                AppNameLogo()
                Column(modifier = Modifier.padding(vertical = 32.dp)) {
                    LoginHeader()
                    LoginForm(
                        email = email,
                        password = password,
                        state = state,
                        notice = notice,
                        onEmailChange = onEmailChange,
                        onPasswordChange = onPasswordChange,
                        onPasswordVisibilityToggle = onPasswordVisibilityToggle,
                        onLoginClick = onLoginClick,
                        onForgotPasswordClick = onForgotPasswordClick
                    )
                }
                Spacer(Modifier)
            }
            LoginFooter(isLoading = state.isLoading, onLoginClick = onLoginClick)
        }
    }
}

@Composable
private fun AppNameLogo() {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun LoginHeader() {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.login_title),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoginForm(
    email: String,
    password: String,
    state: LoginUiState,
    @StringRes notice: Int?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FormMessage(formError = state.formError, notice = notice)
        EmailField(value = email, error = state.emailError, onValueChange = onEmailChange)
        PasswordTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.login_password_label),
            isPasswordVisible = state.isPasswordVisible,
            onVisibilityToggle = onPasswordVisibilityToggle,
            modifier = Modifier.fillMaxWidth(),
            errorMessage = state.passwordError?.let { stringResource(it) },
            keyboardActions = KeyboardActions(onDone = { onLoginClick() })
        )
        ForgotPasswordLink(onClick = onForgotPasswordClick, modifier = Modifier.align(Alignment.End))
    }
}

@Composable
private fun FormMessage(@StringRes formError: Int?, @StringRes notice: Int?) {
    when {
        formError != null -> MessageBanner(message = stringResource(formError))
        notice != null -> MessageBanner(message = stringResource(notice), style = MessageBannerStyle.INFO)
    }
}

@Composable
private fun EmailField(value: String, @StringRes error: Int?, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.login_email_label)) },
        singleLine = true,
        isError = error != null,
        supportingText = if (error != null) { { Text(stringResource(error)) } } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
    )
}

@Composable
private fun ForgotPasswordLink(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text = stringResource(R.string.login_forgot_password), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LoginFooter(isLoading: Boolean, onLoginClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoadingButton(
            text = stringResource(R.string.login_button),
            loadingDescription = stringResource(R.string.login_loading),
            isLoading = isLoading,
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.login_first_login_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
