package com.yudistirosaputro.movieapi.ui.details

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.yudistirosaputro.movieapi.R
import com.yudistirosaputro.movieapi.domain.model.VideoType
import com.yudistirosaputro.movieapi.ui.common.toErrorUi
import com.yudistirosaputro.movieapi.ui.components.BackButton
import com.yudistirosaputro.movieapi.ui.components.Badge
import com.yudistirosaputro.movieapi.ui.components.ErrorState
import com.yudistirosaputro.movieapi.ui.components.MetadataRow
import com.yudistirosaputro.movieapi.ui.components.PrimaryButton
import com.yudistirosaputro.movieapi.ui.components.SecondaryButton
import com.yudistirosaputro.movieapi.ui.components.ShimmerBlock
import com.yudistirosaputro.movieapi.ui.components.ShimmerState
import com.yudistirosaputro.movieapi.ui.components.TestTags
import com.yudistirosaputro.movieapi.ui.components.rememberPressScale
import com.yudistirosaputro.movieapi.ui.components.rememberShimmer
import com.yudistirosaputro.movieapi.ui.navigation.sharedPosterBounds
import com.yudistirosaputro.movieapi.ui.reviews.ReviewItem
import com.yudistirosaputro.movieapi.ui.reviews.ReviewSkeleton
import com.yudistirosaputro.movieapi.ui.theme.MovieColors

private val DetailsTopBarHeight = 56.dp

@Composable
fun DetailsRoute(
    onBack: () -> Unit,
    onSeeAllReviews: (movieId: Int, title: String?) -> Unit,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(DetailsTab.Reviews) }
    val context = LocalContext.current

    FullscreenEffect(enabled = playerState.isFullscreen)
    BackHandler(enabled = playerState.isFullscreen) { viewModel.onFullscreenChanged(false) }

    DetailsScreen(
        uiState = uiState,
        playerState = playerState,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onBack = onBack,
        onRetry = viewModel::retry,
        onRetryReviews = viewModel::retryReviews,
        onPlay = viewModel::onPlay,
        onSeeAllReviews = {
            val title = (uiState as? DetailsUiState.Content)?.details?.title
            onSeeAllReviews(viewModel.movieId, title)
        },
        onFullscreenChange = viewModel::onFullscreenChanged,
        onOpenInYouTube = context::openYouTube,
        player = { key, modifier ->
            YouTubeTrailerPlayer(
                videoKey = key,
                playbackStart = viewModel::playbackStart,
                onProgress = viewModel::onPlaybackProgress,
                onPlayingChanged = viewModel::onPlayingChanged,
                onError = viewModel::onPlayerError,
                modifier = modifier,
            )
        },
    )
}

/**
 * Stateless details screen. [player] is a slot so UI tests can run without a WebView.
 * The media area stays pinned at the top while the content scrolls below it, like a streaming app.
 */
@Composable
fun DetailsScreen(
    uiState: DetailsUiState,
    playerState: PlayerUiState,
    selectedTab: DetailsTab,
    onTabSelected: (DetailsTab) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRetryReviews: () -> Unit,
    onPlay: (String) -> Unit,
    onSeeAllReviews: () -> Unit,
    onFullscreenChange: (Boolean) -> Unit,
    onOpenInYouTube: (String) -> Unit,
    player: @Composable (videoKey: String, modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MovieColors.Black),
    ) {
        if (uiState is DetailsUiState.Error) {
            ErrorState(error = uiState.error, onRetry = onRetry, onBack = onBack)
            DetailsTopBar(title = null, titleAlpha = 0f, onBack = onBack, showFullscreenToggle = false, onEnterFullscreen = {})
            return@BoxWithConstraints
        }

        val fullscreen = playerState.isFullscreen
        val mediaHeight: Dp = if (fullscreen) maxHeight else min(maxWidth * 9f / 16f, maxHeight * 0.5f)
        val content = uiState as? DetailsUiState.Content
        val details = content?.details
        val loading = uiState as? DetailsUiState.Loading
        val title = details?.title ?: loading?.title
        val trailer = (content?.videos as? VideosState.Loaded)?.trailer
        val titleAlpha by remember(listState) {
            derivedStateOf { if (listState.firstVisibleItemIndex > 0) 1f else (listState.firstVisibleItemScrollOffset / 300f).coerceIn(0f, 1f) }
        }

        Column(Modifier.fillMaxSize()) {
            if (!fullscreen) {
                DetailsTopBar(
                    title = title,
                    titleAlpha = titleAlpha,
                    onBack = onBack,
                    showFullscreenToggle = playerState.videoKey != null && !playerState.hasError,
                    onEnterFullscreen = { onFullscreenChange(true) },
                )
            }
            MediaArea(
                movieId = details?.id ?: loading?.movieId,
                imageUrl = details?.let { it.backdropUrl ?: it.posterUrl } ?: loading?.posterUrl,
                title = title.orEmpty(),
                trailerKey = trailer?.key,
                playerState = playerState,
                onPlay = onPlay,
                onFullscreenChange = onFullscreenChange,
                onOpenInYouTube = onOpenInYouTube,
                player = player,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mediaHeight),
            )
            if (!fullscreen) {
                when (uiState) {
                    is DetailsUiState.Loading -> DetailsSkeleton()
                    is DetailsUiState.Content -> DetailsContent(
                        content = uiState,
                        playerState = playerState,
                        selectedTab = selectedTab,
                        onTabSelected = onTabSelected,
                        onPlay = onPlay,
                        onSeeAllReviews = onSeeAllReviews,
                        onRetryReviews = onRetryReviews,
                        listState = listState,
                    )
                    is DetailsUiState.Error -> Unit
                }
            }
        }
    }
}

@Composable
private fun DetailsTopBar(
    title: String?,
    titleAlpha: Float,
    onBack: () -> Unit,
    showFullscreenToggle: Boolean,
    onEnterFullscreen: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MovieColors.Black)
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .height(DetailsTopBarHeight)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(onClick = onBack)
        Text(
            text = title.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = MovieColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .alpha(titleAlpha),
        )
        // Lives in the bar, not on the video, so it never covers the YouTube player's own controls.
        if (showFullscreenToggle) FullscreenToggle(fullscreen = false, onToggle = onEnterFullscreen)
    }
}

@Composable
private fun MediaArea(
    movieId: Int?,
    imageUrl: String?,
    title: String,
    trailerKey: String?,
    playerState: PlayerUiState,
    onPlay: (String) -> Unit,
    onFullscreenChange: (Boolean) -> Unit,
    onOpenInYouTube: (String) -> Unit,
    player: @Composable (videoKey: String, modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val videoKey = playerState.videoKey
    Box(
        modifier = modifier
            .background(MovieColors.Black)
            .testTag(TestTags.MEDIA_AREA),
        contentAlignment = Alignment.Center,
    ) {
        when {
            videoKey != null && playerState.hasError -> PlayerErrorOverlay(onOpenInYouTube = { onOpenInYouTube(videoKey) })
            videoKey != null -> {
                player(
                    videoKey,
                    Modifier
                        .fillMaxHeight()
                        .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
                )
                if (playerState.isFullscreen) {
                    // On a phone held sideways this corner is outside the 16:9 video, clear of its controls.
                    FullscreenToggle(
                        fullscreen = true,
                        onToggle = { onFullscreenChange(false) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                    )
                }
            }
            else -> Backdrop(
                movieId = movieId,
                imageUrl = imageUrl,
                title = title,
                trailerKey = trailerKey,
                onPlay = onPlay,
            )
        }
    }
}

@Composable
private fun Backdrop(
    movieId: Int?,
    imageUrl: String?,
    title: String,
    trailerKey: String?,
    onPlay: (String) -> Unit,
) {
    var imageFailed by remember(imageUrl) { mutableStateOf(imageUrl == null) }
    val sharedModifier = if (movieId != null) Modifier.sharedPosterBounds(movieId) else Modifier
    Box(
        modifier = sharedModifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (imageFailed) {
            Icon(Icons.Rounded.Movie, contentDescription = null, tint = MovieColors.SurfaceHigh, modifier = Modifier.size(72.dp))
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = stringResource(R.string.cd_backdrop, title),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { imageFailed = true },
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1f to MovieColors.Black,
                    ),
                ),
        )
        if (trailerKey != null) {
            PlayCircle(onClick = { onPlay(trailerKey) })
        }
    }
}

@Composable
private fun PlayCircle(onClick: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, pressedScale = 0.9f)
    val description = stringResource(R.string.cd_play_trailer)
    Box(
        modifier = Modifier
            .size(64.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(CircleShape)
            .background(MovieColors.Scrim)
            .border(2.dp, MovieColors.TextPrimary, CircleShape)
            .clickable(interactionSource = interactionSource, indication = androidx.compose.material3.ripple(), role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = MovieColors.TextPrimary, modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun FullscreenToggle(fullscreen: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
            .clip(CircleShape)
            .background(MovieColors.Scrim),
    ) {
        Icon(
            imageVector = if (fullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
            contentDescription = stringResource(if (fullscreen) R.string.cd_exit_fullscreen else R.string.cd_enter_fullscreen),
            tint = MovieColors.TextPrimary,
        )
    }
}

@Composable
private fun PlayerErrorOverlay(onOpenInYouTube: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = MovieColors.TextSecondary, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.player_error_title),
            style = MaterialTheme.typography.titleMedium,
            color = MovieColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        PrimaryButton(
            text = stringResource(R.string.player_open_in_youtube),
            onClick = onOpenInYouTube,
            icon = Icons.AutoMirrored.Rounded.OpenInNew,
        )
    }
}

@Composable
private fun DetailsContent(
    content: DetailsUiState.Content,
    playerState: PlayerUiState,
    selectedTab: DetailsTab,
    onTabSelected: (DetailsTab) -> Unit,
    onPlay: (String) -> Unit,
    onSeeAllReviews: () -> Unit,
    onRetryReviews: () -> Unit,
    listState: LazyListState,
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = bottomInset + 24.dp),
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .testTag(TestTags.DETAILS_CONTENT),
    ) {
        item(key = "title", contentType = "title") {
            TitleBlock(details = content.details, videos = content.videos, onPlay = onPlay)
        }
        item(key = "tabs", contentType = "tabs") {
            DetailsTabRow(selected = selectedTab, onSelected = onTabSelected)
        }
        when (selectedTab) {
            DetailsTab.Reviews -> reviewsTab(content.reviews, onSeeAllReviews, onRetryReviews)
            DetailsTab.Trailers -> trailersTab(content.videos, playerState.videoKey, onPlay)
        }
    }
}

@Composable
private fun TitleBlock(details: MovieDetailsUi, videos: VideosState, onPlay: (String) -> Unit) {
    val ratingText = details.rating?.let { rating ->
        stringResource(R.string.details_rating, rating) +
            (details.voteCount?.let { " " + stringResource(R.string.details_votes, it) } ?: "")
    }
    val metadata = listOfNotNull(details.year, details.runtimeMinutes?.let { runtimeText(it) }, ratingText)
    val badges = listOfNotNull(details.language?.let { Badge(it, stringResource(R.string.cd_original_language, it)) })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 20.dp),
    ) {
        Text(
            text = details.title.ifBlank { stringResource(R.string.untitled) },
            style = MaterialTheme.typography.headlineMedium,
            color = MovieColors.TextPrimary,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(8.dp))
        MetadataRow(items = metadata, badges = badges)
        if (details.tagline != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = details.tagline,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MovieColors.TextSecondary,
            )
        }
        Spacer(Modifier.height(16.dp))
        TrailerAction(videos = videos, onPlay = onPlay)
        Spacer(Modifier.height(16.dp))
        Text(
            text = details.overview ?: stringResource(R.string.details_no_overview),
            style = MaterialTheme.typography.bodyMedium,
            color = if (details.overview != null) MovieColors.TextPrimary else MovieColors.TextSecondary,
        )
        if (details.genres != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.details_genres, details.genres),
                style = MaterialTheme.typography.bodySmall,
                color = MovieColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun TrailerAction(videos: VideosState, onPlay: (String) -> Unit) {
    when (videos) {
        VideosState.Loading -> {
            val shimmer = rememberShimmer()
            ShimmerBlock(shimmer, Modifier.fillMaxWidth().height(48.dp), MaterialTheme.shapes.extraSmall)
        }
        is VideosState.Loaded -> if (videos.trailer != null) {
            PrimaryButton(
                text = stringResource(R.string.details_play_trailer),
                onClick = { onPlay(videos.trailer.key) },
                icon = Icons.Rounded.PlayArrow,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.PLAY_TRAILER),
            )
        } else {
            TrailerUnavailable()
        }
        VideosState.Unavailable -> TrailerUnavailable()
    }
}

@Composable
private fun TrailerUnavailable() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MovieColors.Surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Rounded.VideocamOff, contentDescription = null, tint = MovieColors.TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.details_trailer_unavailable), style = MaterialTheme.typography.labelLarge, color = MovieColors.TextSecondary)
    }
}

@Composable
private fun runtimeText(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours > 0 && rest > 0 -> stringResource(R.string.details_runtime_hours_minutes, hours, rest)
        hours > 0 -> stringResource(R.string.details_runtime_hours, hours)
        else -> stringResource(R.string.details_runtime_minutes, rest)
    }
}

@Composable
private fun DetailsTabRow(selected: DetailsTab, onSelected: (DetailsTab) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        HorizontalDivider(color = MovieColors.Divider, thickness = 1.dp)
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            DetailsTab.entries.forEach { tab ->
                val isSelected = tab == selected
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .heightIn(min = 48.dp)
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelected(tab) }),
                ) {
                    // The indicator sits on top of the selected tab, not below it.
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(if (isSelected) MovieColors.Accent else Color.Transparent),
                    )
                    Text(
                        text = stringResource(if (tab == DetailsTab.Reviews) R.string.details_tab_reviews else R.string.details_tab_trailers),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isSelected) MovieColors.TextPrimary else MovieColors.TextSecondary,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}

private fun LazyListScope.reviewsTab(
    state: ReviewsPreviewState,
    onSeeAllReviews: () -> Unit,
    onRetryReviews: () -> Unit,
) {
    when (state) {
        ReviewsPreviewState.Loading -> item(key = "reviews_loading") {
            val shimmer = rememberShimmer()
            Column(Modifier.testTag(TestTags.SKELETON)) { repeat(3) { ReviewSkeleton(shimmer) } }
        }
        is ReviewsPreviewState.Error -> item(key = "reviews_error") {
            InlineMessage(
                icon = state.error.toErrorUi().icon,
                text = stringResource(state.error.toErrorUi().title),
                actionLabel = stringResource(R.string.action_retry),
                onAction = onRetryReviews,
            )
        }
        is ReviewsPreviewState.Loaded -> if (state.reviews.isEmpty()) {
            item(key = "reviews_empty") {
                InlineMessage(icon = Icons.Rounded.RateReview, text = stringResource(R.string.reviews_empty_title))
            }
        } else {
            itemsIndexed(state.reviews, key = { _, review -> "review_${review.id}" }) { index, review ->
                if (index > 0) HorizontalDivider(color = MovieColors.Divider, thickness = 1.dp)
                ReviewItem(review)
            }
            item(key = "see_all") {
                SecondaryButton(
                    text = stringResource(R.string.details_see_all_reviews, state.totalResults),
                    onClick = onSeeAllReviews,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

private fun LazyListScope.trailersTab(state: VideosState, playingKey: String?, onPlay: (String) -> Unit) {
    when (state) {
        VideosState.Loading -> item(key = "videos_loading") {
            val shimmer = rememberShimmer()
            Column(Modifier.testTag(TestTags.SKELETON), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(2) { VideoSkeleton(shimmer) }
            }
        }
        VideosState.Unavailable -> item(key = "videos_empty") {
            InlineMessage(icon = Icons.Rounded.VideocamOff, text = stringResource(R.string.details_no_videos))
        }
        is VideosState.Loaded -> if (state.videos.isEmpty()) {
            item(key = "videos_empty") {
                InlineMessage(icon = Icons.Rounded.VideocamOff, text = stringResource(R.string.details_no_videos))
            }
        } else {
            itemsIndexed(state.videos, key = { _, video -> "video_${video.key}" }) { _, video ->
                VideoItem(video = video, playing = video.key == playingKey, onClick = { onPlay(video.key) })
            }
        }
    }
}

@Composable
private fun VideoItem(video: VideoUi, playing: Boolean, onClick: () -> Unit) {
    val title = video.title.ifBlank { stringResource(videoTypeLabel(video.type)) }
    val description = stringResource(R.string.cd_video_thumbnail, title)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MovieColors.Surface),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = description,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MovieColors.Scrim)
                    .border(1.5.dp, MovieColors.TextPrimary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = MovieColors.TextPrimary, modifier = Modifier.size(28.dp))
            }
            if (playing) {
                Text(
                    text = stringResource(R.string.details_now_playing).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MovieColors.TextPrimary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(MovieColors.Accent, MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MovieColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        val typeLabel = stringResource(videoTypeLabel(video.type))
        Text(
            text = if (video.official) typeLabel + "  ·  " + stringResource(R.string.video_official) else typeLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MovieColors.TextSecondary,
        )
    }
}

private fun videoTypeLabel(type: VideoType): Int = when (type) {
    VideoType.Trailer -> R.string.video_type_trailer
    VideoType.Teaser -> R.string.video_type_teaser
    VideoType.Clip -> R.string.video_type_clip
    VideoType.Featurette -> R.string.video_type_featurette
    VideoType.BehindTheScenes -> R.string.video_type_behind_the_scenes
    VideoType.Bloopers -> R.string.video_type_bloopers
    VideoType.Other -> R.string.video_type_other
}

@Composable
private fun InlineMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MovieColors.TextSecondary, modifier = Modifier.size(40.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, color = MovieColors.TextSecondary, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            PrimaryButton(text = actionLabel, onClick = onAction, icon = Icons.Rounded.Refresh)
        }
    }
}

@Composable
private fun VideoSkeleton(shimmer: ShimmerState) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ShimmerBlock(shimmer, Modifier.fillMaxWidth().aspectRatio(16f / 9f), MaterialTheme.shapes.extraSmall)
        ShimmerBlock(shimmer, Modifier.fillMaxWidth(0.7f).height(14.dp), MaterialTheme.shapes.extraSmall)
    }
}

@Composable
private fun DetailsSkeleton() {
    val shimmer = rememberShimmer()
    val description = stringResource(R.string.loading)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp)
            .testTag(TestTags.SKELETON)
            .semantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ShimmerBlock(shimmer, Modifier.fillMaxWidth(0.7f).height(30.dp), MaterialTheme.shapes.extraSmall)
        ShimmerBlock(shimmer, Modifier.fillMaxWidth(0.45f).height(16.dp), MaterialTheme.shapes.extraSmall)
        Spacer(Modifier.height(4.dp))
        ShimmerBlock(shimmer, Modifier.fillMaxWidth().height(48.dp), MaterialTheme.shapes.extraSmall)
        Spacer(Modifier.height(4.dp))
        repeat(4) { line ->
            ShimmerBlock(
                state = shimmer,
                modifier = Modifier
                    .fillMaxWidth(if (line == 3) 0.55f else 1f)
                    .height(14.dp),
                shape = MaterialTheme.shapes.extraSmall,
            )
        }
    }
}

/** Hides the system bars and turns the screen sideways while the trailer is full screen. */
@Composable
private fun FullscreenEffect(enabled: Boolean) {
    val activity = LocalActivity.current ?: return
    DisposableEffect(enabled) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        if (enabled) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        onDispose {
            // During a configuration change the new activity re-applies full screen itself.
            if (enabled && !activity.isChangingConfigurations) {
                controller.show(WindowInsetsCompat.Type.systemBars())
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }
}
