package com.example.paypulse.ui.users.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.user.User
import com.example.paypulse.domain.user.UserRole
import com.example.paypulse.domain.user.UserStatus

@Composable
internal fun UserRow(user: User, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(user = user)
        UserNameAndEmail(user = user, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
        Column(horizontalAlignment = Alignment.End) {
            RoleBadge(role = user.role)
            StatusText(status = user.status)
        }
    }
}

@Composable
internal fun UserAvatar(user: User) {
    val colors = MaterialTheme.colorScheme
    val highlighted = user.role == UserRole.ADMIN && user.status != UserStatus.INACTIVE
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (highlighted) colors.primary else colors.surfaceVariant)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = user.initials,
            style = MaterialTheme.typography.labelLarge,
            color = if (highlighted) colors.onPrimary else colors.onSurfaceVariant
        )
    }
}

@Composable
private fun UserNameAndEmail(user: User, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val isInactive = user.status == UserStatus.INACTIVE
    Column(modifier = modifier) {
        Text(
            text = user.fullName,
            style = MaterialTheme.typography.titleSmall,
            color = if (isInactive) colors.onSurfaceVariant else colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RoleBadge(role: UserRole) {
    val colors = MaterialTheme.colorScheme
    val isAdmin = role == UserRole.ADMIN
    Surface(
        shape = CircleShape,
        color = if (isAdmin) colors.primaryContainer else colors.surfaceVariant,
        contentColor = if (isAdmin) colors.onPrimaryContainer else colors.onSurfaceVariant
    ) {
        Text(
            text = stringResource(if (isAdmin) R.string.users_role_admin else R.string.users_role_user),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun StatusText(status: UserStatus) {
    val colors = MaterialTheme.colorScheme
    val (label, color) = when (status) {
        UserStatus.ACTIVE -> R.string.users_status_active to colors.tertiary
        UserStatus.PENDING_ACTIVATION -> R.string.users_status_pending to colors.secondary
        UserStatus.INACTIVE -> R.string.users_status_inactive to colors.onSurfaceVariant
    }
    Text(
        text = stringResource(label),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier.padding(top = 4.dp)
    )
}