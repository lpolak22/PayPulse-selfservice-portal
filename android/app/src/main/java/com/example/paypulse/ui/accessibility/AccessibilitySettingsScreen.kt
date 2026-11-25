package com.example.paypulse.ui.accessibility

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paypulse.R
import com.example.paypulse.domain.accessibility.AccessibilitySettings
import com.example.paypulse.ui.components.PayPulseTopBar
import com.example.paypulse.ui.theme.PayPulseTheme

@Composable
fun AccessibilitySettingsRoute(
    onMenuClick: () -> Unit,
    viewModel: AccessibilitySettingsViewModel = viewModel(factory = AccessibilitySettingsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AccessibilitySettingsScreen(
        state = state,
        onLargerTextChange = viewModel::onLargerTextChange,
        onHighContrastChange = viewModel::onHighContrastChange,
        onMenuClick = onMenuClick
    )
}

@Composable
fun AccessibilitySettingsScreen(
    state: AccessibilitySettings,
    onLargerTextChange: (Boolean) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier, topBar = { PayPulseTopBar(onMenuClick = onMenuClick) }) { padding ->
        AccessibilitySettingsContent(
            state = state,
            onLargerTextChange = onLargerTextChange,
            onHighContrastChange = onHighContrastChange,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun AccessibilitySettingsContent(
    state: AccessibilitySettings,
    onLargerTextChange: (Boolean) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.accessibility_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )
        SettingSwitchRow(
            R.string.accessibility_larger_text,
            R.string.accessibility_larger_text_description,
            state.largerText,
            onLargerTextChange
        )
        SettingSwitchRow(
            R.string.accessibility_high_contrast,
            R.string.accessibility_high_contrast_description,
            state.highContrast,
            onHighContrastChange
        )
        PreviewCard()
    }
}

@Composable
private fun SettingSwitchRow(
    @StringRes label: Int,
    @StringRes description: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(label), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun PreviewCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.accessibility_preview_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            Text(text = stringResource(R.string.accessibility_preview_body), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccessibilitySettingsScreenDefaultPreview() {
    PayPulseTheme {
        AccessibilitySettingsScreen(AccessibilitySettings(), {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AccessibilitySettingsScreenEnabledPreview() {
    val state = AccessibilitySettings(largerText = true, highContrast = true)
    PayPulseTheme(accessibility = state) {
        AccessibilitySettingsScreen(state, {}, {}, {})
    }
}
