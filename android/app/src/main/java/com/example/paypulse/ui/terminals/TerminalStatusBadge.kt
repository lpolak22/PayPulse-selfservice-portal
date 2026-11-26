package com.example.paypulse.ui.terminals

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.terminal.TerminalStatus

private const val BADGE_BACKGROUND_ALPHA = 0.14f

enum class BadgeTone { POSITIVE, PENDING, NEUTRAL, NEGATIVE }

data class TerminalBadge(
    val tone: BadgeTone,
    @get:StringRes val label: Int? = null,
    val rawText: String? = null
)

fun terminalBadge(status: TerminalStatus?, rawStatus: String): TerminalBadge = when {
    status != null -> TerminalBadge(tone = status.tone(), label = status.badgeLabel())
    rawStatus.isNotBlank() -> TerminalBadge(tone = BadgeTone.NEUTRAL, rawText = rawStatus)
    else -> TerminalBadge(tone = BadgeTone.NEUTRAL, label = R.string.terminals_status_unknown)
}

private fun TerminalStatus.tone(): BadgeTone = when (this) {
    TerminalStatus.ACTIVE -> BadgeTone.POSITIVE
    TerminalStatus.PREPARED, TerminalStatus.AUTHENTICATED -> BadgeTone.PENDING
    TerminalStatus.SUSPENDED, TerminalStatus.BLOCKED -> BadgeTone.NEGATIVE
    TerminalStatus.INACTIVE, TerminalStatus.DEACTIVATED, TerminalStatus.UNKNOWN -> BadgeTone.NEUTRAL
}

@Composable
fun TerminalStatusBadge(badge: TerminalBadge, modifier: Modifier = Modifier) {
    val color = badge.tone.color()
    Surface(color = color.copy(alpha = BADGE_BACKGROUND_ALPHA), shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = badge.label?.let { stringResource(it) } ?: badge.rawText.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun BadgeTone.color(): Color = when (this) {
    BadgeTone.POSITIVE -> MaterialTheme.colorScheme.tertiary
    BadgeTone.PENDING -> MaterialTheme.colorScheme.secondary
    BadgeTone.NEGATIVE -> MaterialTheme.colorScheme.error
    BadgeTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
}
