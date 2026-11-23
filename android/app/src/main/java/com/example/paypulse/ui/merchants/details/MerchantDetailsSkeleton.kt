package com.example.paypulse.ui.merchants.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.ui.components.SkeletonBlock
import com.example.paypulse.ui.components.rememberSkeletonAlpha

private const val SKELETON_ROWS = 4

@Composable
fun MerchantDetailsSkeleton(modifier: Modifier = Modifier) {
    val alphaState = rememberSkeletonAlpha()
    val alpha = { alphaState.value }
    val description = stringResource(R.string.loading)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonBlock(alpha, Modifier.fillMaxWidth().height(200.dp), RoundedCornerShape(24.dp))
        SkeletonBlock(alpha, Modifier.fillMaxWidth().height(40.dp), MaterialTheme.shapes.small)
        SkeletonBlock(alpha, Modifier.fillMaxWidth().height(56.dp), MaterialTheme.shapes.medium)
        repeat(SKELETON_ROWS) { RowSkeleton(alpha) }
    }
}

@Composable
private fun RowSkeleton(alpha: () -> Float) {
    val shape = MaterialTheme.shapes.small
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SkeletonBlock(alpha, Modifier.size(56.dp), RoundedCornerShape(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBlock(alpha, Modifier.width(160.dp).height(16.dp), shape)
            SkeletonBlock(alpha, Modifier.width(200.dp).height(14.dp), shape)
            SkeletonBlock(alpha, Modifier.width(120.dp).height(12.dp), shape)
        }
    }
}
