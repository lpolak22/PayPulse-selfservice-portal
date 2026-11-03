package com.example.paypulse.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.paypulse.R

@Composable
fun DashboardLoading(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.loading)
    Box(modifier = modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = description })
    }
}

@Composable
fun DashboardMessage(text: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            TextButton(onClick = onRetry) { Text(stringResource(R.string.dashboard_retry)) }
        }
    }
}
