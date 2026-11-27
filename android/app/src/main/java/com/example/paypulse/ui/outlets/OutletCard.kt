package com.example.paypulse.ui.outlets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.outlet.OutletStatus
import com.example.paypulse.ui.components.CroatianFormat

@Composable
fun OutletCard(outlet: Outlet, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NameAndStatus(name = outlet.name, status = outlet.status)
            Row(modifier = Modifier.fillMaxWidth()) {
                LabeledValue(R.string.outlets_id_label, outlet.outletId, Modifier.weight(1f))
                LabeledValue(R.string.outlets_city_label, outlet.city, Modifier.weight(1f))
            }
            LabeledValue(R.string.outlets_address_label, outlet.fullAddress())
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            TerminalCount(count = outlet.terminalCount)
        }
    }
}

private fun Outlet.fullAddress(): String? =
    listOfNotNull(address, postalCode).joinToString(", ").ifEmpty { null }

@Composable
private fun NameAndStatus(name: String, status: OutletStatus?) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        OutletStatusBadge(status = status)
    }
}

@Composable
fun OutletStatusBadge(status: OutletStatus?, modifier: Modifier = Modifier) {
    Surface(color = status.badgeBackground(), shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = stringResource(status.badgeLabel()),
            style = MaterialTheme.typography.labelMedium,
            color = status.badgeColor(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun LabeledValue(label: Int, value: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value ?: stringResource(R.string.outlets_missing_value),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TerminalCount(count: Long) {
    val countText = CroatianFormat.count(count.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.outlets_terminals, countText),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
