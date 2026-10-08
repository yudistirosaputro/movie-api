package com.yudistirosaputro.movieapi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yudistirosaputro.movieapi.ui.theme.MovieColors

/** Poster-only 2:3 card; the title lives in contentDescription, or on the card itself when there is no poster. */
@Composable
fun PosterCard(
    title: String,
    posterUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)
    var imageFailed by remember(posterUrl) { mutableStateOf(posterUrl == null) }
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .aspectRatio(2f / 3f)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MovieColors.Surface)
            .clickable(interactionSource = interactionSource, indication = ripple(), onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = title
                role = Role.Button
                onClick { onClick(); true }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (imageFailed) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MovieColors.TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp),
            )
        } else {
            AsyncImage(
                model = posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { imageFailed = true },
            )
        }
    }
}

@Composable
fun PosterSkeleton(shimmer: ShimmerState, modifier: Modifier = Modifier) {
    ShimmerBlock(
        state = shimmer,
        modifier = modifier.aspectRatio(2f / 3f),
        shape = MaterialTheme.shapes.extraSmall,
    )
}
