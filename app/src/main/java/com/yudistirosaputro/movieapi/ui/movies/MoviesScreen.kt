package com.yudistirosaputro.movieapi.ui.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.yudistirosaputro.movieapi.R
import com.yudistirosaputro.movieapi.ui.common.FooterState
import com.yudistirosaputro.movieapi.ui.common.ListState
import com.yudistirosaputro.movieapi.ui.components.BackButton
import com.yudistirosaputro.movieapi.ui.components.EmptyState
import com.yudistirosaputro.movieapi.ui.components.ErrorState
import com.yudistirosaputro.movieapi.ui.components.MovieTopBar
import com.yudistirosaputro.movieapi.ui.components.PagingFooter
import com.yudistirosaputro.movieapi.ui.components.PosterCard
import com.yudistirosaputro.movieapi.ui.components.PosterSkeleton
import com.yudistirosaputro.movieapi.ui.components.TestTags
import com.yudistirosaputro.movieapi.ui.components.TopBarHeight
import com.yudistirosaputro.movieapi.ui.components.rememberShimmer
import com.yudistirosaputro.movieapi.ui.components.topBarAlpha
import com.yudistirosaputro.movieapi.ui.navigation.sharedPosterBounds
import com.yudistirosaputro.movieapi.ui.theme.MovieColors
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun MoviesRoute(
    onBack: () -> Unit,
    onMovieClick: (MovieUi) -> Unit,
    viewModel: MoviesViewModel = hiltViewModel(),
) {
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val gridState = rememberLazyGridState()
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect(viewModel::onFirstVisibleItemChanged)
    }
    MoviesScreen(
        genreName = viewModel.genreName,
        movies = movies,
        onBack = onBack,
        onMovieClick = onMovieClick,
        gridState = gridState,
    )
}

@Composable
fun MoviesScreen(
    genreName: String,
    movies: LazyPagingItems<MovieUi>,
    onBack: () -> Unit,
    onMovieClick: (MovieUi) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = topInset + TopBarHeight + 4.dp, bottom = bottomInset + 16.dp)
    val listState = ListState.from(movies.loadState, movies.itemCount)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MovieColors.Background),
    ) {
        val columns = if (maxWidth < 600.dp) 3 else (maxWidth / 150.dp).toInt().coerceAtLeast(4)
        val horizontalSafe = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))

        when (listState) {
            ListState.Loading -> MoviesSkeleton(columns, contentPadding, horizontalSafe)
            ListState.Empty -> EmptyState(
                icon = Icons.Rounded.MovieFilter,
                title = stringResource(R.string.movies_empty_title),
                message = stringResource(R.string.movies_empty_message),
                onRetry = movies::refresh,
            )
            is ListState.Error -> ErrorState(error = listState.error, onRetry = movies::retry, onBack = onBack)
            ListState.Content -> LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = gridState,
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = horizontalSafe
                    .fillMaxSize()
                    .testTag(TestTags.MOVIE_GRID),
            ) {
                items(
                    count = movies.itemCount,
                    key = movies.itemKey { it.id },
                    contentType = movies.itemContentType { "poster" },
                ) { index ->
                    val movie = movies[index] ?: return@items
                    val title = movie.title.ifBlank { stringResource(R.string.untitled) }
                    PosterCard(
                        title = title,
                        posterUrl = movie.posterUrl,
                        onClick = { onMovieClick(movie) },
                        modifier = Modifier.sharedPosterBounds(movie.id),
                    )
                }
                item(key = "footer", span = { GridItemSpan(maxLineSpan) }, contentType = "footer") {
                    PagingFooter(state = FooterState.from(movies.loadState), onRetry = movies::retry)
                }
            }
        }

        MovieTopBar(
            backgroundAlpha = gridState.topBarAlpha(),
            navigationIcon = { BackButton(onClick = onBack) },
            title = {
                Text(
                    text = genreName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MovieColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { heading() },
                )
            },
        )
    }
}

@Composable
private fun MoviesSkeleton(columns: Int, contentPadding: PaddingValues, modifier: Modifier) {
    val shimmer = rememberShimmer()
    val description = stringResource(R.string.loading)
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false,
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.SKELETON)
            .semantics { contentDescription = description },
    ) {
        items(count = columns * 5) { PosterSkeleton(shimmer) }
    }
}
