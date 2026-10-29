package com.example.paypulse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.paypulse.R

enum class MessageBannerStyle { ERROR, INFO }

@Composable
fun MessageBanner(
    message: String,
    modifier: Modifier = Modifier,
    style: MessageBannerStyle = MessageBannerStyle.ERROR
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = containerColor(style),
        contentColor = contentColor(style)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (style == MessageBannerStyle.INFO) {
                Icon(painter = painterResource(R.drawable.ic_check_circle), contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Text(
                text = message,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun containerColor(style: MessageBannerStyle): Color = when (style) {
    MessageBannerStyle.ERROR -> MaterialTheme.colorScheme.errorContainer
    MessageBannerStyle.INFO -> MaterialTheme.colorScheme.secondaryContainer
}

@Composable
private fun contentColor(style: MessageBannerStyle): Color = when (style) {
    MessageBannerStyle.ERROR -> MaterialTheme.colorScheme.onErrorContainer
    MessageBannerStyle.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
}
