package com.example.paypulse.ui.merchants.details

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.merchant.MerchantTerminal
import com.example.paypulse.domain.outlet.Outlet

private const val TILE_BACKGROUND_ALPHA = 0.5f

@Composable
fun OutletRow(outlet: Outlet, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val terminals = outlet.terminalCount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    val meta = listOfNotNull(
        outlet.outletId,
        pluralStringResource(R.plurals.merchant_details_terminal_count, terminals, terminals)
    ).joinToString(" · ")
    RelatedItemRow(
        icon = R.drawable.ic_location_on,
        title = outlet.name,
        lines = listOfNotNull(outlet.addressLine(), meta),
        statusColor = outlet.status.color(),
        statusLabel = stringResource(outlet.status.label()),
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun TerminalRow(terminal: MerchantTerminal, onClick: () -> Unit, modifier: Modifier = Modifier) {
    RelatedItemRow(
        icon = R.drawable.ic_point_of_sale,
        title = terminal.terminalId ?: stringResource(R.string.merchant_details_missing_value),
        lines = listOfNotNull(terminal.outletName),
        statusColor = terminal.status.color(),
        statusLabel = stringResource(terminal.status.label()),
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun RelatedItemRow(
    @DrawableRes icon: Int,
    title: String,
    lines: List<String>,
    statusColor: Color,
    statusLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .padding(vertical = 16.dp)
        ) {
            IconTile(icon = icon)
            RowTexts(title = title, lines = lines, modifier = Modifier.weight(1f))
            StatusDot(color = statusColor, label = statusLabel)
            Icon(painter = painterResource(R.drawable.ic_chevron_right), contentDescription = null)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun IconTile(@DrawableRes icon: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = TILE_BACKGROUND_ALPHA),
                RoundedCornerShape(16.dp)
            )
    ) {
        Icon(painter = painterResource(icon), contentDescription = null)
    }
}

@Composable
private fun RowTexts(title: String, lines: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        lines.forEachIndexed { index, line ->
            Text(
                text = line,
                style = if (index == 0) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatusDot(color: Color, label: String) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color, CircleShape)
            .semantics { contentDescription = label }
    )
}
