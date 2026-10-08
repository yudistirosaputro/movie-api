package com.yudistirosaputro.movieapi.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Null outside the nav host (previews, UI tests), where shared elements are simply skipped. */
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

private data class PosterSharedKey(val movieId: Int)

/** Morphs a poster card in the grid into the details media area. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedPosterBounds(movieId: Int): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(sharedScope) {
        this@sharedPosterBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key = PosterSharedKey(movieId)),
            animatedVisibilityScope = visibilityScope,
            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(4.dp)),
        )
    }
}
