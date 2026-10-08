package com.yudistirosaputro.movieapi.ui.details

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.Outcome
import com.yudistirosaputro.movieapi.domain.model.VideoType
import com.yudistirosaputro.movieapi.domain.usecase.GetMovieDetailsUseCase
import com.yudistirosaputro.movieapi.domain.usecase.GetMovieVideosUseCase
import com.yudistirosaputro.movieapi.domain.usecase.GetReviewsPreviewUseCase
import com.yudistirosaputro.movieapi.testing.FakeMovieRepository
import com.yudistirosaputro.movieapi.testing.FakeMovieRepository.Companion.sampleVideo
import com.yudistirosaputro.movieapi.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Robolectric because SavedStateHandle.toRoute decodes navigation arguments through android.os.Bundle. */
@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class DetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMovieRepository()

    private fun handle(extra: Map<String, Any?> = emptyMap()) =
        SavedStateHandle(mapOf<String, Any?>("movieId" to 969681, "title" to "Spider-Man", "posterUrl" to "https://img/p.jpg") + extra)

    private fun viewModel(handle: SavedStateHandle = handle()) = DetailsViewModel(
        savedStateHandle = handle,
        getMovieDetails = GetMovieDetailsUseCase(repository),
        getMovieVideos = GetMovieVideosUseCase(repository),
        getReviewsPreview = GetReviewsPreviewUseCase(repository),
    )

    @Test
    fun `reads the movie from the navigation arguments`() {
        assertEquals(969681, viewModel().movieId)
    }

    @Test
    fun `content combines details, the selected trailer and the reviews preview`() = runTest {
        viewModel().uiState.test {
            val content = awaitItem() as DetailsUiState.Content
            assertEquals("Spider-Man: Brand New Day", content.details.title)
            assertEquals("2026", content.details.year)
            assertEquals("EN", content.details.language)
            assertEquals("Action, Adventure", content.details.genres)
            assertEquals("trailer", (content.videos as VideosState.Loaded).trailer?.key)
            val reviews = content.reviews as ReviewsPreviewState.Loaded
            assertEquals(3, reviews.reviews.size)
            assertEquals(25, reviews.totalResults)
        }
    }

    @Test
    fun `404 shows the not found error`() = runTest {
        repository.details = Outcome.Failure(AppError.NotFound())

        viewModel().uiState.test {
            assertEquals(DetailsUiState.Error(AppError.NotFound()), awaitItem())
        }
    }

    @Test
    fun `a failing videos call never breaks the details`() = runTest {
        repository.videos = Outcome.Failure(AppError.Server(500))

        viewModel().uiState.test {
            val content = awaitItem() as DetailsUiState.Content
            assertEquals(VideosState.Unavailable, content.videos)
        }
    }

    @Test
    fun `no trailer or teaser means no trailer, but other videos stay listed`() = runTest {
        repository.videos = Outcome.Success(listOf(sampleVideo("clip", VideoType.Clip)))

        viewModel().uiState.test {
            val videos = (awaitItem() as DetailsUiState.Content).videos as VideosState.Loaded
            assertNull(videos.trailer)
            assertEquals(listOf("clip"), videos.videos.map { it.key })
        }
    }

    @Test
    fun `a failing reviews call shows a reviews error and can be retried`() = runTest {
        repository.reviews = Outcome.Failure(AppError.RateLimited())
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(ReviewsPreviewState.Error(AppError.RateLimited()), (awaitItem() as DetailsUiState.Content).reviews)
            repository.reviews = FakeMovieRepository().reviews
            viewModel.retryReviews()
            var reviews = (awaitItem() as DetailsUiState.Content).reviews
            while (reviews is ReviewsPreviewState.Loading) reviews = (awaitItem() as DetailsUiState.Content).reviews
            assertTrue(reviews is ReviewsPreviewState.Loaded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry reloads everything after an error`() = runTest {
        repository.details = Outcome.Failure(AppError.NoInternet)
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(DetailsUiState.Error(AppError.NoInternet), awaitItem())
            repository.details = Outcome.Success(FakeMovieRepository.sampleDetails())
            viewModel.retry()
            // Loading can be conflated away when the fake answers synchronously; what matters is where it ends.
            var state = awaitItem()
            while (state is DetailsUiState.Loading) state = awaitItem()
            assertTrue(state is DetailsUiState.Content)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(2, repository.detailsCalls)
    }

    @Test
    fun `missing runtime and overview stay null for the screen to fall back on`() = runTest {
        repository.details = Outcome.Success(FakeMovieRepository.sampleDetails(runtime = null, overview = null, voteCount = 0))

        viewModel().uiState.test {
            val details = (awaitItem() as DetailsUiState.Content).details
            assertNull(details.runtimeMinutes)
            assertNull(details.overview)
            assertNull(details.rating)
            assertNull(details.voteCount)
        }
    }

    @Test
    fun `playing a video, its progress and full screen live in the saved state`() = runTest {
        val handle = handle()
        val viewModel = viewModel(handle)

        viewModel.onPlay("trailer")
        viewModel.onPlaybackProgress(42.5f)
        viewModel.onFullscreenChanged(true)

        assertEquals(PlayerUiState(videoKey = "trailer", isFullscreen = true), viewModel.playerState.value)
        assertEquals(PlaybackStart(seconds = 42.5f, autoPlay = true), viewModel.playbackStart())

        // A new ViewModel over the same handle is what process death restores.
        val restored = viewModel(handle)
        assertEquals(PlayerUiState(videoKey = "trailer", isFullscreen = true), restored.playerState.value)
        assertEquals(42.5f, restored.playbackStart().seconds)
    }

    @Test
    fun `a user pause is remembered so a recreated player stays paused`() {
        val viewModel = viewModel()
        viewModel.onPlay("trailer")

        viewModel.onPlayingChanged(false)

        assertFalse(viewModel.playbackStart().autoPlay)
    }

    @Test
    fun `a player error leaves full screen and can be cleared by playing again`() {
        val viewModel = viewModel()
        viewModel.onPlay("trailer")
        viewModel.onFullscreenChanged(true)

        viewModel.onPlayerError()
        assertEquals(PlayerUiState(videoKey = "trailer", isFullscreen = false, hasError = true), viewModel.playerState.value)

        viewModel.onPlay("trailer")
        assertFalse(viewModel.playerState.value.hasError)
    }

    @Test
    fun `switching videos restarts playback from the beginning`() {
        val viewModel = viewModel()
        viewModel.onPlay("trailer")
        viewModel.onPlaybackProgress(80f)

        viewModel.onPlay("clip")

        assertEquals("clip", viewModel.playerState.value.videoKey)
        assertEquals(0f, viewModel.playbackStart().seconds)
    }
}
