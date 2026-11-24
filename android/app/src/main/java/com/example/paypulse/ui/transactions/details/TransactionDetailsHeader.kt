package com.example.paypulse.ui.transactions.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paypulse.R

private const val BADGE_BACKGROUND_ALPHA = 0.12f

@Composable
fun TransactionDetailsHeader(details: TransactionDetailsModel, modifier: Modifier = Modifier) {
    val statusColor = details.status.badgeColor()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatusIcon(icon = details.status.badgeIcon(), color = statusColor)
        Text(
            text = details.amount,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = stringResource(details.status.badgeLabel()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = statusColor
        )
        Text(
            text = stringResource(R.string.transaction_details_date_time, details.date, details.time),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusIcon(icon: Int, color: Color) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(color.copy(alpha = BADGE_BACKGROUND_ALPHA), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
    }
}
