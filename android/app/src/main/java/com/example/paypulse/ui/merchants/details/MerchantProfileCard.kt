package com.example.paypulse.ui.merchants.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.merchant.MerchantDetails
import com.example.paypulse.domain.merchant.MerchantStatus
import com.example.paypulse.ui.merchants.badgeColor
import com.example.paypulse.ui.merchants.badgeLabel

private const val CARD_BACKGROUND_ALPHA = 0.5f

@Composable
fun MerchantProfileCard(details: MerchantDetails, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = CARD_BACKGROUND_ALPHA),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InitialsAvatar(name = details.name)
                NameAndStatus(name = details.name, status = details.status)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                ProfileField(
                    label = stringResource(R.string.merchants_id_label),
                    value = details.merchantId ?: stringResource(R.string.merchants_id_missing),
                    modifier = Modifier.weight(1f)
                )
                ProfileField(stringResource(R.string.merchants_oib_label), details.oib, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InitialsAvatar(name: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
            .clearAndSetSemantics { }
    ) {
        Text(
            text = merchantInitials(name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun NameAndStatus(name: String, status: MerchantStatus?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(8.dp).background(status.badgeColor(), CircleShape))
            Text(
                text = stringResource(status.badgeLabel()),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = status.badgeColor()
            )
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}
