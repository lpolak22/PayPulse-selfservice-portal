package com.example.paypulse.ui.merchants

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
import com.example.paypulse.domain.merchant.Merchant
import com.example.paypulse.domain.merchant.MerchantStatus
import com.example.paypulse.ui.components.CroatianFormat

@Composable
fun MerchantCard(merchant: Merchant, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NameAndStatus(name = merchant.name, status = merchant.status)
            IdAndOib(merchantId = merchant.merchantId, oib = merchant.oib)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            OutletsAndTerminals(outletCount = merchant.outletCount, terminalCount = merchant.terminalCount)
        }
    }
}

@Composable
private fun NameAndStatus(name: String, status: MerchantStatus?) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        StatusBadge(status = status)
    }
}

@Composable
fun StatusBadge(status: MerchantStatus?, modifier: Modifier = Modifier) {
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
private fun IdAndOib(merchantId: String?, oib: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        LabeledValue(
            label = stringResource(R.string.merchants_id_label),
            value = merchantId ?: stringResource(R.string.merchants_id_missing),
            modifier = Modifier.weight(1f)
        )
        LabeledValue(label = stringResource(R.string.merchants_oib_label), value = oib, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LabeledValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun OutletsAndTerminals(outletCount: Long, terminalCount: Long) {
    val outletText = CroatianFormat.count(outletCount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
    val terminalText = CroatianFormat.count(terminalCount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.merchants_outlets_and_terminals, outletText, terminalText),
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
