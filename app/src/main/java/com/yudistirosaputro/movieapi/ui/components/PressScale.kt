package com.yudistirosaputro.movieapi.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue

/** Returns a State so callers read it inside graphicsLayer and the press animation skips recomposition. */
@Composable
fun rememberPressScale(interactionSource: InteractionSource, pressedScale: Float = 0.96f): State<Float> {
    val pressed by interactionSource.collectIsPressedAsState()
    return animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "pressScale",
    )
}
