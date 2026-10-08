package com.yudistirosaputro.movieapi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yudistirosaputro.movieapi.R
import com.yudistirosaputro.movieapi.ui.theme.MovieColors

val TopBarHeight: Dp = 64.dp

/** Transparent over content; [backgroundAlpha] fades it to solid black as the content scrolls under it. */
@Composable
fun MovieTopBar(
    backgroundAlpha: State<Float>,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    title: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind { drawRect(MovieColors.Black.copy(alpha = backgroundAlpha.value)) }
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(TopBarHeight)
                .padding(start = if (navigationIcon == null) 16.dp else 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationIcon?.invoke()
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { title() }
            actions()
        }
    }
}

@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.cd_back),
            tint = MovieColors.TextPrimary,
        )
    }
}

private const val FADE_DISTANCE_PX = 240f

@Composable
fun LazyListState.topBarAlpha(): State<Float> = remember(this) {
    derivedStateOf {
        if (firstVisibleItemIndex > 0) 1f else (firstVisibleItemScrollOffset / FADE_DISTANCE_PX).coerceIn(0f, 1f)
    }
}

@Composable
fun LazyGridState.topBarAlpha(): State<Float> = remember(this) {
    derivedStateOf {
        if (firstVisibleItemIndex > 0) 1f else (firstVisibleItemScrollOffset / FADE_DISTANCE_PX).coerceIn(0f, 1f)
    }
}

/** Solid bar for screens without scrollable content under them, such as status screens. */
@Composable
fun rememberStaticAlpha(alpha: Float): State<Float> = remember(alpha) { derivedStateOf { alpha } }
