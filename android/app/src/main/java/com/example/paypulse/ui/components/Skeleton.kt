package com.example.paypulse.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer

private const val PULSE_MIN_ALPHA = 0.45f
private const val PULSE_DURATION_MILLIS = 900

@Composable
fun rememberSkeletonAlpha(): State<Float> = rememberInfiniteTransition(label = "skeleton").animateFloat(
    initialValue = PULSE_MIN_ALPHA,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(PULSE_DURATION_MILLIS), RepeatMode.Reverse),
    label = "skeletonAlpha"
)

@Composable
fun SkeletonBlock(
    alpha: () -> Float,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium
) {
    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha() }
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}
