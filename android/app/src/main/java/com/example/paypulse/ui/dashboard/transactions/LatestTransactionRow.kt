package com.example.paypulse.ui.dashboard.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.ui.dashboard.color
import com.example.paypulse.ui.dashboard.label

@Composable
fun LatestTransactionRow(row: LatestTransactionRowModel, modifier: Modifier = Modifier) {
    val brand = stringResource(row.brand.label())
    val status = stringResource(row.status.label())
    val description = stringResource(
        R.string.dashboard_transaction_description,
        brand, row.lastDigits, row.outletName, row.time, row.amount, status
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clearAndSetSemantics { contentDescription = description }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CardIcon()
        CardAndPlace(
            card = stringResource(R.string.dashboard_transaction_card, brand, row.lastDigits),
            place = stringResource(R.string.dashboard_transaction_place_time, row.outletName, row.time),
            modifier = Modifier.weight(1f)
        )
        AmountAndStatus(row = row, status = status)
    }
}

@Composable
private fun CardIcon() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_credit_card),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun CardAndPlace(card: String, place: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = card,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = place,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AmountAndStatus(row: LatestTransactionRowModel, status: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(text = row.amount, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(
            text = status,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = row.status.color()
        )
    }
}
