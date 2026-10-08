package com.yudistirosaputro.movieapi.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.yudistirosaputro.movieapi.domain.model.Outcome
import com.yudistirosaputro.movieapi.domain.model.Review
import com.yudistirosaputro.movieapi.domain.model.Video
import com.yudistirosaputro.movieapi.domain.model.map
import com.yudistirosaputro.movieapi.domain.usecase.GetMovieDetailsUseCase
import com.yudistirosaputro.movieapi.domain.usecase.GetMovieVideosUseCase
import com.yudistirosaputro.movieapi.domain.usecase.GetReviewsPreviewUseCase
import com.yudistirosaputro.movieapi.ui.navigation.DetailsDestination
import com.yudistirosaputro.movieapi.ui.reviews.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getMovieDetails: GetMovieDetailsUseCase,
    private val getMovieVideos: GetMovieVideosUseCase,
    private val getReviewsPreview: GetReviewsPreviewUseCase,
) : ViewModel() {

    private val destination = savedStateHandle.toRoute<DetailsDestination>()

    val movieId: Int = destination.movieId

    private val details = MutableStateFlow<Outcome<MovieDetailsUi>?>(null)
    private val videos = MutableStateFlow<VideosState>(VideosState.Loading)
    private val reviews = MutableStateFlow<ReviewsPreviewState>(ReviewsPreviewState.Loading)
    private val playerError = MutableStateFlow(false)

    private val initialState = DetailsUiState.Loading(destination.movieId, destination.title, destination.posterUrl)

    /** Videos and reviews load independently, so neither can break the details screen. */
    val uiState: StateFlow<DetailsUiState> = combine(details, videos, reviews) { details, videos, reviews ->
        when (details) {
            null -> initialState
            is Outcome.Failure -> DetailsUiState.Error(details.error)
            is Outcome.Success -> DetailsUiState.Content(details.value, videos, reviews)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), initialState)

    /** Lives in SavedStateHandle so the playing video and full-screen mode survive rotation and process death. */
    val playerState: StateFlow<PlayerUiState> = combine(
        savedStateHandle.getStateFlow<String?>(KEY_VIDEO, null),
        savedStateHandle.getStateFlow(KEY_FULLSCREEN, false),
        playerError,
    ) { key, fullscreen, error ->
        PlayerUiState(videoKey = key, isFullscreen = fullscreen && key != null, hasError = error)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        PlayerUiState(videoKey = savedStateHandle[KEY_VIDEO], isFullscreen = savedStateHandle[KEY_FULLSCREEN] ?: false),
    )

    private var detailsJob: Job? = null
    private var videosJob: Job? = null
    private var reviewsJob: Job? = null

    init {
        loadAll()
    }

    fun retry() = loadAll()

    fun retryReviews() = loadReviews()

    fun onPlay(videoKey: String) {
        if (videoKey == savedStateHandle.get<String>(KEY_VIDEO) && !playerError.value) return
        savedStateHandle[KEY_SECONDS] = 0f
        savedStateHandle[KEY_PLAYING] = true
        playerError.value = false
        savedStateHandle[KEY_VIDEO] = videoKey
    }

    fun onPlaybackProgress(seconds: Float) {
        savedStateHandle[KEY_SECONDS] = seconds
    }

    fun onPlayingChanged(playing: Boolean) {
        savedStateHandle[KEY_PLAYING] = playing
    }

    fun onPlayerError() {
        playerError.value = true
        savedStateHandle[KEY_FULLSCREEN] = false
    }

    fun onFullscreenChanged(fullscreen: Boolean) {
        savedStateHandle[KEY_FULLSCREEN] = fullscreen
    }

    /** Read once when the player is (re)created; progress isn't part of [playerState] to avoid recomposing per tick. */
    fun playbackStart(): PlaybackStart = PlaybackStart(
        seconds = savedStateHandle[KEY_SECONDS] ?: 0f,
        autoPlay = savedStateHandle[KEY_PLAYING] ?: true,
    )

    private fun loadAll() {
        loadDetails()
        loadVideos()
        loadReviews()
    }

    private fun loadDetails() {
        detailsJob?.cancel()
        details.value = null
        detailsJob = viewModelScope.launch {
            details.value = getMovieDetails(movieId).map { it.toUi() }
        }
    }

    private fun loadVideos() {
        videosJob?.cancel()
        videos.value = VideosState.Loading
        videosJob = viewModelScope.launch {
            videos.value = when (val result = getMovieVideos(movieId)) {
                is Outcome.Success -> VideosState.Loaded(
                    trailer = result.value.trailer?.toUi(),
                    videos = result.value.youTubeVideos.map(Video::toUi),
                )
                is Outcome.Failure -> VideosState.Unavailable
            }
        }
    }

    private fun loadReviews() {
        reviewsJob?.cancel()
        reviews.value = ReviewsPreviewState.Loading
        reviewsJob = viewModelScope.launch {
            reviews.value = when (val result = getReviewsPreview(movieId)) {
                is Outcome.Success -> ReviewsPreviewState.Loaded(
                    reviews = result.value.reviews.map(Review::toUi),
                    totalResults = result.value.totalResults,
                )
                is Outcome.Failure -> ReviewsPreviewState.Error(result.error)
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val KEY_VIDEO = "player_video_key"
        const val KEY_SECONDS = "player_seconds"
        const val KEY_PLAYING = "player_playing"
        const val KEY_FULLSCREEN = "player_fullscreen"
    }
}
