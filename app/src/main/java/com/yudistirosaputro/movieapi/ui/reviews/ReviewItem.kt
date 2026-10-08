package com.yudistirosaputro.movieapi.ui.reviews

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yudistirosaputro.movieapi.R
import com.yudistirosaputro.movieapi.ui.components.ShimmerBlock
import com.yudistirosaputro.movieapi.ui.components.ShimmerState
import com.yudistirosaputro.movieapi.ui.theme.MovieColors

const val REVIEW_COLLAPSED_LINES = 6

@Composable
fun ReviewItem(review: ReviewUi, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable(review.id) { mutableStateOf(false) }
    var overflows by remember(review.id) { mutableStateOf(false) }
    val author = review.author.ifBlank { stringResource(R.string.review_anonymous) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(initial = review.initial, avatarUrl = review.avatarUrl, author = author)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = author,
                    style = MaterialTheme.typography.titleSmall,
                    color = MovieColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (review.date != null) {
                    Text(text = review.date, style = MaterialTheme.typography.bodySmall, color = MovieColors.TextSecondary)
                }
            }
            if (review.rating != null) {
                Spacer(Modifier.width(8.dp))
                RatingChip(review.rating)
            }
        }
        if (review.content.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MovieColors.TextPrimary.copy(alpha = 0.86f),
                maxLines = if (expanded) Int.MAX_VALUE else REVIEW_COLLAPSED_LINES,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { result -> if (!expanded) overflows = result.hasVisualOverflow },
                modifier = Modifier.animateContentSize(),
            )
            if (overflows || expanded) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.heightIn(min = 48.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp),
                ) {
                    Text(
                        text = stringResource(if (expanded) R.string.review_show_less else R.string.review_read_more),
                        style = MaterialTheme.typography.labelLarge,
                        color = MovieColors.TextPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun Avatar(initial: String, avatarUrl: String?, author: String) {
    var imageFailed by remember(avatarUrl) { mutableStateOf(avatarUrl == null) }
    val description = stringResource(R.string.cd_avatar, author)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MovieColors.SurfaceHigh)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(initial, style = MaterialTheme.typography.titleMedium, color = MovieColors.TextPrimary)
        if (!imageFailed) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { imageFailed = true },
            )
        }
    }
}

@Composable
private fun RatingChip(rating: String) {
    val description = stringResource(R.string.cd_rating, rating)
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MovieColors.SurfaceHigh)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Rounded.Star, contentDescription = null, tint = MovieColors.Accent, modifier = Modifier.size(14.dp))
        Text(rating, style = MaterialTheme.typography.labelMedium, color = MovieColors.TextPrimary)
    }
}

@Composable
fun ReviewSkeleton(shimmer: ShimmerState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShimmerBlock(shimmer, Modifier.size(40.dp), CircleShape)
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShimmerBlock(shimmer, Modifier.width(140.dp).height(14.dp), MaterialTheme.shapes.extraSmall)
                ShimmerBlock(shimmer, Modifier.width(90.dp).height(12.dp), MaterialTheme.shapes.extraSmall)
            }
        }
        repeat(3) { line ->
            ShimmerBlock(
                state = shimmer,
                modifier = Modifier
                    .fillMaxWidth(if (line == 2) 0.6f else 1f)
                    .height(12.dp),
                shape = MaterialTheme.shapes.extraSmall,
            )
        }
    }
}
