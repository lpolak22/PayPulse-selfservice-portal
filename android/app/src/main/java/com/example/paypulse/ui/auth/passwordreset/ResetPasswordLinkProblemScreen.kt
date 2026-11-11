package com.example.paypulse.ui.auth.passwordreset

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.ui.components.FullHeightScrollColumn

private val ButtonContentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)

@Composable
fun ResetPasswordLinkProblemScreen(
    problem: ResetPasswordPage.LinkProblem,
    onRetryClick: () -> Unit,
    onRequestNewLinkClick: () -> Unit,
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
                Text(
                    text = stringResource(R.string.reset_password_title),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(problem.message),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (problem.canRetry) {
                    PrimaryButton(R.string.reset_password_retry, onRetryClick)
                } else {
                    PrimaryButton(R.string.reset_password_request_new_link, onRequestNewLinkClick)
                }
                OutlinedButton(
                    onClick = onBackToLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = ButtonContentPadding
                ) {
                    ButtonLabel(R.string.reset_password_back_to_login)
                }
            }
        }
    }
}

@Composable
private fun PrimaryButton(@StringRes text: Int, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), contentPadding = ButtonContentPadding) {
        ButtonLabel(text)
    }
}

@Composable
private fun ButtonLabel(@StringRes text: Int) {
    Text(text = stringResource(text), style = MaterialTheme.typography.titleMedium)
}
