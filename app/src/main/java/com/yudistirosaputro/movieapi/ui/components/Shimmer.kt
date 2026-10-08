package com.yudistirosaputro.movieapi.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import com.yudistirosaputro.movieapi.ui.theme.MovieColors

@Stable
class ShimmerState internal constructor(internal val progress: State<Float>)

/** One transition per skeleton, so every block in it sweeps in sync. */
@Composable
fun rememberShimmer(): ShimmerState {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress",
    )
    return remember(progress) { ShimmerState(progress) }
}

/** The animated value is read in the draw phase only, so a running shimmer never recomposes. */
fun Modifier.shimmer(state: ShimmerState, shape: Shape = RectangleShape): Modifier = clip(shape).drawBehind {
    drawRect(MovieColors.ShimmerBase)
    val band = size.width.coerceAtLeast(size.height)
    val x = -band + state.progress.value * (size.width + 2 * band)
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(MovieColors.ShimmerBase, MovieColors.ShimmerHighlight, MovieColors.ShimmerBase),
            start = Offset(x - band, 0f),
            end = Offset(x + band, size.height),
        ),
    )
}

@Composable
fun ShimmerBlock(state: ShimmerState, modifier: Modifier = Modifier, shape: Shape = RectangleShape) {
    Box(modifier.shimmer(state, shape))
}
