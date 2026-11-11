package com.example.paypulse.ui.auth.passwordreset

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@Composable
fun ForgotPasswordScreen(
    email: String,
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
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
                if (state.isSent) {
                    Header(title = R.string.forgot_password_sent_title, subtitle = null)
                    MessageBanner(
                        message = stringResource(R.string.forgot_password_sent_message),
                        style = MessageBannerStyle.INFO
                    )
                } else {
                    Header(title = R.string.forgot_password_title, subtitle = R.string.forgot_password_subtitle)
                    state.formError?.let { MessageBanner(message = stringResource(it)) }
                    EmailField(value = email, error = state.emailError, onValueChange = onEmailChange, onDone = onSubmitClick)
                }
            }
            Footer(state = state, onSubmitClick = onSubmitClick, onBackToLoginClick = onBackToLoginClick)
        }
    }
}

@Composable
private fun Header(@StringRes title: Int, @StringRes subtitle: Int?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(title),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        subtitle?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmailField(value: String, @StringRes error: Int?, onValueChange: (String) -> Unit, onDone: () -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.forgot_password_email_label)) },
        singleLine = true,
        isError = error != null,
        supportingText = if (error != null) { { Text(stringResource(error)) } } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() })
    )
}

@Composable
private fun Footer(state: ForgotPasswordUiState, onSubmitClick: () -> Unit, onBackToLoginClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.isSent) {
            OutlinedButton(onClick = onBackToLoginClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.forgot_password_back_to_login))
            }
        } else {
            LoadingButton(
                text = stringResource(R.string.forgot_password_submit),
                loadingDescription = stringResource(R.string.forgot_password_loading),
                isLoading = state.isLoading,
                onClick = onSubmitClick,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = onBackToLoginClick) {
                Text(text = stringResource(R.string.forgot_password_back_to_login), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
