package com.yudistirosaputro.movieapi.ui.genres

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yudistirosaputro.movieapi.R
import com.yudistirosaputro.movieapi.ui.components.EmptyState
import com.yudistirosaputro.movieapi.ui.components.ErrorState
import com.yudistirosaputro.movieapi.ui.components.MovieTopBar
import com.yudistirosaputro.movieapi.ui.components.ShimmerBlock
import com.yudistirosaputro.movieapi.ui.components.TestTags
import com.yudistirosaputro.movieapi.ui.components.TopBarHeight
import com.yudistirosaputro.movieapi.ui.components.Wordmark
import com.yudistirosaputro.movieapi.ui.components.rememberShimmer
import com.yudistirosaputro.movieapi.ui.components.topBarAlpha
import com.yudistirosaputro.movieapi.ui.theme.MovieApiTheme
import com.yudistirosaputro.movieapi.ui.theme.MovieColors

@Composable
fun GenresRoute(
    onGenreClick: (GenreUi) -> Unit,
    onAboutClick: () -> Unit,
    viewModel: GenresViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GenresScreen(
        uiState = uiState,
        onGenreClick = onGenreClick,
        onAboutClick = onAboutClick,
        onRetry = viewModel::retry,
    )
}

@Composable
fun GenresScreen(
    uiState: GenresUiState,
    onGenreClick: (GenreUi) -> Unit,
    onAboutClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentPadding = PaddingValues(top = topInset + TopBarHeight + 8.dp, bottom = bottomInset + 32.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MovieColors.Background)
            .background(
                Brush.radialGradient(
                    colors = listOf(MovieColors.Accent.copy(alpha = 0.16f), MovieColors.Background.copy(alpha = 0f)),
                    center = androidx.compose.ui.geometry.Offset(x = with(LocalDensity.current) { 180.dp.toPx() }, y = 0f),
                    radius = with(LocalDensity.current) { 520.dp.toPx() },
                ),
            ),
    ) {
        when (uiState) {
            GenresUiState.Loading -> GenresSkeleton(Modifier.padding(contentPadding))
            is GenresUiState.Content -> GenreList(uiState.genres, listState, contentPadding, onGenreClick)
            GenresUiState.Empty -> EmptyState(
                icon = Icons.Rounded.MovieFilter,
                title = stringResource(R.string.genres_empty_title),
                message = stringResource(R.string.genres_empty_message),
                onRetry = onRetry,
            )
            is GenresUiState.Error -> ErrorState(error = uiState.error, onRetry = onRetry)
        }

        MovieTopBar(
            backgroundAlpha = listState.topBarAlpha(),
            title = { Wordmark() },
            actions = {
                IconButton(onClick = onAboutClick) {
                    Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.cd_about), tint = MovieColors.TextPrimary)
                }
            },
        )
    }
}

@Composable
private fun GenreList(
    genres: List<GenreUi>,
    listState: LazyListState,
    contentPadding: PaddingValues,
    onGenreClick: (GenreUi) -> Unit,
) {
    LazyColumn(
        state = listState,
        contentPadding = contentPadding,
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTags.GENRE_LIST),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item(key = "heading", contentType = "heading") {
            Text(
                text = stringResource(R.string.genres_heading).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MovieColors.TextTertiary,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )
        }
        items(genres, key = { it.id }, contentType = { "genre" }) { genre ->
            GenreItem(genre = genre, onClick = { onGenreClick(genre) })
        }
    }
}

@Composable
private fun GenreItem(genre: GenreUi, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Text(
        text = genre.name,
        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
        fontWeight = if (pressed) FontWeight.Black else FontWeight.Medium,
        color = if (pressed) MovieColors.TextPrimary else MovieColors.TextSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
    )
}

@Composable
private fun GenresSkeleton(modifier: Modifier = Modifier) {
    val shimmer = rememberShimmer()
    val description = stringResource(R.string.loading)
    val widths = remember { listOf(132, 108, 164, 96, 148, 120, 180, 104, 140, 116, 156) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.SKELETON)
            .semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        widths.forEach { width ->
            ShimmerBlock(
                state = shimmer,
                modifier = Modifier
                    .width(width.dp)
                    .height(24.dp),
                shape = MaterialTheme.shapes.extraSmall,
            )
        }
    }
}

@Preview
@Composable
private fun GenresContentPreview() {
    MovieApiTheme {
        GenresScreen(
            uiState = GenresUiState.Content(listOf(GenreUi(28, "Action"), GenreUi(12, "Adventure"), GenreUi(16, "Animation"))),
            onGenreClick = {},
            onAboutClick = {},
            onRetry = {},
        )
    }
}
