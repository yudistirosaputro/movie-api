package com.yudistirosaputro.movieapi.ui.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material3.HorizontalDivider
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
import com.yudistirosaputro.movieapi.ui.components.TestTags
import com.yudistirosaputro.movieapi.ui.components.TopBarHeight
import com.yudistirosaputro.movieapi.ui.components.rememberShimmer
import com.yudistirosaputro.movieapi.ui.components.topBarAlpha
import com.yudistirosaputro.movieapi.ui.theme.MovieColors
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ReviewsRoute(
    onBack: () -> Unit,
    viewModel: ReviewsViewModel = hiltViewModel(),
) {
    val reviews = viewModel.reviews.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect(viewModel::onFirstVisibleItemChanged)
    }
    ReviewsScreen(movieTitle = viewModel.movieTitle, reviews = reviews, onBack = onBack, listState = listState)
}

@Composable
fun ReviewsScreen(
    movieTitle: String?,
    reviews: LazyPagingItems<ReviewUi>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentPadding = PaddingValues(top = topInset + TopBarHeight, bottom = bottomInset + 16.dp)
    val horizontalSafe = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MovieColors.Background),
    ) {
        when (val state = ListState.from(reviews.loadState, reviews.itemCount)) {
            ListState.Loading -> ReviewsSkeleton(Modifier.padding(contentPadding).then(horizontalSafe))
            ListState.Empty -> EmptyState(
                icon = Icons.Rounded.RateReview,
                title = stringResource(R.string.reviews_empty_title),
                message = stringResource(R.string.reviews_empty_message),
            )
            is ListState.Error -> ErrorState(error = state.error, onRetry = reviews::retry, onBack = onBack)
            ListState.Content -> LazyColumn(
                state = listState,
                contentPadding = contentPadding,
                modifier = horizontalSafe
                    .fillMaxSize()
                    .testTag(TestTags.REVIEW_LIST),
            ) {
                items(
                    count = reviews.itemCount,
                    key = reviews.itemKey { it.id },
                    contentType = reviews.itemContentType { "review" },
                ) { index ->
                    val review = reviews[index] ?: return@items
                    if (index > 0) HorizontalDivider(color = MovieColors.Divider, thickness = 1.dp)
                    ReviewItem(review)
                }
                item(key = "footer", contentType = "footer") {
                    PagingFooter(state = FooterState.from(reviews.loadState), onRetry = reviews::retry)
                }
            }
        }

        MovieTopBar(
            backgroundAlpha = listState.topBarAlpha(),
            navigationIcon = { BackButton(onClick = onBack) },
            title = {
                Column {
                    Text(
                        text = stringResource(R.string.reviews_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MovieColors.TextPrimary,
                        modifier = Modifier.semantics { heading() },
                    )
                    if (!movieTitle.isNullOrBlank()) {
                        Text(
                            text = movieTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MovieColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun ReviewsSkeleton(modifier: Modifier = Modifier) {
    val shimmer = rememberShimmer()
    val description = stringResource(R.string.loading)
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.SKELETON)
            .semantics { contentDescription = description },
    ) {
        repeat(5) { ReviewSkeleton(shimmer) }
    }
}
