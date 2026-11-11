package com.example.paypulse.ui.auth.activation

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
fun ActivationLinkProblemScreen(
    problem: ActivationPage.LinkProblem,
    onRetryClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            ProblemMessage(
                message = stringResource(problem.message),
                modifier = Modifier.weight(1f)
            )
            ProblemActions(
                canRetry = problem.canRetry,
                onRetryClick = onRetryClick,
                onBackToLoginClick = onBackToLoginClick
            )
        }
    }
}

@Composable
private fun ProblemMessage(message: String, modifier: Modifier = Modifier) {
    FullHeightScrollColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.activation_title),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProblemActions(canRetry: Boolean, onRetryClick: () -> Unit, onBackToLoginClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (canRetry) {
            Button(onClick = onRetryClick, modifier = Modifier.fillMaxWidth(), contentPadding = ButtonContentPadding) {
                ButtonLabel(R.string.activation_retry)
            }
            OutlinedButton(onClick = onBackToLoginClick, modifier = Modifier.fillMaxWidth(), contentPadding = ButtonContentPadding) {
                ButtonLabel(R.string.activation_back_to_login)
            }
        } else {
            Button(onClick = onBackToLoginClick, modifier = Modifier.fillMaxWidth(), contentPadding = ButtonContentPadding) {
                ButtonLabel(R.string.activation_back_to_login)
            }
        }
    }
}

@Composable
private fun ButtonLabel(@StringRes text: Int) {
    Text(text = stringResource(text), style = MaterialTheme.typography.titleMedium)
}
