package com.example.paypulse.ui.merchants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.ui.components.SkeletonBlock
import com.example.paypulse.ui.components.rememberSkeletonAlpha

private const val SKELETON_CARDS = 4

@Composable
fun MerchantsSkeleton(modifier: Modifier = Modifier) {
    val alphaState = rememberSkeletonAlpha()
    val alpha = { alphaState.value }
    val description = stringResource(R.string.loading)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(SKELETON_CARDS) { MerchantCardSkeleton(alpha) }
    }
}

@Composable
private fun MerchantCardSkeleton(alpha: () -> Float) {
    val shape = MaterialTheme.shapes.small
    OutlinedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                SkeletonBlock(alpha, Modifier.width(180.dp).height(18.dp), shape)
                SkeletonBlock(alpha, Modifier.width(72.dp).height(18.dp), shape)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(48.dp)) {
                SkeletonBlock(alpha, Modifier.width(90.dp).height(32.dp), shape)
                SkeletonBlock(alpha, Modifier.width(110.dp).height(32.dp), shape)
            }
            SkeletonBlock(alpha, Modifier.width(140.dp).height(14.dp), shape)
        }
    }
}
