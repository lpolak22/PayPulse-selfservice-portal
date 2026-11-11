package com.example.paypulse.ui.users.form

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.user.User
import com.example.paypulse.ui.users.list.UserAvatar

@Composable
internal fun UserHeader(user: User) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        UserAvatar(user = user)
        Text(text = user.fullName, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
internal fun ActiveSwitchRow(isActive: Boolean, enabled: Boolean, onActiveChange: (Boolean) -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = isActive, enabled = enabled, role = Role.Switch, onValueChange = onActiveChange)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.users_active_label), style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(if (enabled) R.string.users_active_description else R.string.users_pending_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = isActive, onCheckedChange = null, enabled = enabled)
        }
    }
}

@Composable
internal fun EditUserDialogs(state: EditUserUiState, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val name = state.user?.fullName ?: return
    when (state.dialog) {
        EditUserDialog.DEACTIVATE -> ConfirmationDialog(
            message = stringResource(R.string.users_deactivate_message, name),
            confirmText = R.string.users_deactivate_confirm,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
        EditUserDialog.DELETE -> ConfirmationDialog(
            message = stringResource(R.string.users_delete_message, name),
            confirmText = R.string.users_delete_confirm,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
        null -> Unit
    }
}

@Composable
private fun ConfirmationDialog(message: String, @StringRes confirmText: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.users_dialog_title)) },
        text = { Text(message) },
        confirmButton = { Button(onClick = onConfirm) { Text(stringResource(confirmText)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.users_dialog_cancel)) } }
    )
}