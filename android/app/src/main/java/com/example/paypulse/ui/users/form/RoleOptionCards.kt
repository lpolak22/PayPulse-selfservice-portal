package com.example.paypulse.ui.users.form

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.user.UserRole

@Composable
internal fun RoleOptionCards(selected: UserRole, onSelect: (UserRole) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.users_role_label), style = MaterialTheme.typography.titleSmall)
        RoleOptionCard(
            role = UserRole.ADMIN,
            title = R.string.users_role_admin,
            description = R.string.users_role_admin_description,
            selected = selected,
            onSelect = onSelect
        )
        RoleOptionCard(
            role = UserRole.USER,
            title = R.string.users_role_user,
            description = R.string.users_role_user_description,
            selected = selected,
            onSelect = onSelect
        )
    }
}

@Composable
private fun RoleOptionCard(
    role: UserRole,
    @StringRes title: Int,
    @StringRes description: Int,
    selected: UserRole,
    onSelect: (UserRole) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val isSelected = role == selected
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) colors.primaryContainer else colors.surface,
        border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = isSelected, onClick = { onSelect(role) }, role = Role.RadioButton)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isSelected, onClick = null)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = stringResource(title), style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(description),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}