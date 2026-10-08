package com.yudistirosaputro.movieapi.ui.details

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.VideoType
import com.yudistirosaputro.movieapi.ui.components.TestTags
import com.yudistirosaputro.movieapi.ui.reviews.ReviewUi
import com.yudistirosaputro.movieapi.ui.theme.MovieApiTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var played: String? = null
    private var retries = 0
    private var backs = 0
    private var seeAll = 0

    private val details = MovieDetailsUi(
        id = 1, title = "Spider-Man: Brand New Day", tagline = "A brand new day starts now.", overview = "A hero rises.",
        backdropUrl = null, posterUrl = null, year = "2026", runtimeMinutes = 145, rating = "8.3", voteCount = "4,316",
        language = "EN", genres = "Action, Adventure",
    )
    private val trailer = VideoUi("abc", "Official Trailer", VideoType.Trailer, official = true, thumbnailUrl = null)
    private val reviews = ReviewsPreviewState.Loaded(
        reviews = (1..3).map { ReviewUi("$it", "Critic $it", "C", null, "8.0", "Jul 28, 2026", "Review $it") },
        totalResults = 18,
    )

    private fun show(state: DetailsUiState, player: PlayerUiState = PlayerUiState(), tab: DetailsTab = DetailsTab.Reviews) {
        composeRule.setContent {
            MovieApiTheme {
                DetailsScreen(
                    uiState = state,
                    playerState = player,
                    selectedTab = tab,
                    onTabSelected = {},
                    onBack = { backs++ },
                    onRetry = { retries++ },
                    onRetryReviews = {},
                    onPlay = { played = it },
                    onSeeAllReviews = { seeAll++ },
                    onFullscreenChange = {},
                    onOpenInYouTube = {},
                    player = { key, modifier -> Box(modifier.testTag("fake_player_$key")) },
                )
            }
        }
    }

    private fun content(
        details: MovieDetailsUi = this.details,
        videos: VideosState = VideosState.Loaded(trailer, listOf(trailer)),
        reviews: ReviewsPreviewState = this.reviews,
    ) = DetailsUiState.Content(details, videos, reviews)

    @Test
    fun loadingShowsTheSkeletonAndTheTitleFromNavigation() {
        show(DetailsUiState.Loading(1, "Spider-Man: Brand New Day", null))
        composeRule.onNodeWithTag(TestTags.SKELETON).assertIsDisplayed()
    }

    @Test
    fun contentShowsTitleMetadataTaglineOverviewAndGenres() {
        show(content())

        composeRule.onNodeWithText("2026  ·  2h 25m  ·  ★ 8.3 (4,316)").assertIsDisplayed()
        composeRule.onNodeWithText("A brand new day starts now.").assertIsDisplayed()
        composeRule.onNodeWithText("A hero rises.").assertIsDisplayed()
        composeRule.onNodeWithText("Genres: Action, Adventure").assertIsDisplayed()
    }

    @Test
    fun playTrailerStartsTheSelectedTrailer() {
        show(content())

        composeRule.onNodeWithTag(TestTags.PLAY_TRAILER).performClick()

        assertEquals("abc", played)
    }

    @Test
    fun playTrailerIsHiddenWithoutATrailer() {
        show(content(videos = VideosState.Loaded(trailer = null, videos = emptyList())))

        composeRule.onNodeWithTag(TestTags.PLAY_TRAILER).assertDoesNotExist()
        composeRule.onNodeWithText("Trailer not available").assertIsDisplayed()
    }

    @Test
    fun aFailedVideosCallStillShowsTheDetails() {
        show(content(videos = VideosState.Unavailable))

        composeRule.onNodeWithTag(TestTags.PLAY_TRAILER).assertDoesNotExist()
        composeRule.onNodeWithText("Trailer not available").assertIsDisplayed()
        composeRule.onNodeWithText("A hero rises.").assertIsDisplayed()
    }

    @Test
    fun missingFieldsDegradeToFallbacks() {
        show(content(details = details.copy(overview = null, runtimeMinutes = null, tagline = null, genres = null, year = null, rating = null, voteCount = null, language = null)))

        composeRule.onNodeWithText("No overview available").assertIsDisplayed()
        composeRule.onNodeWithText("Genres:", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("·", substring = true).assertDoesNotExist()
    }

    @Test
    fun playingAVideoPutsThePlayerInTheMediaArea() {
        show(content(), player = PlayerUiState(videoKey = "abc"))
        composeRule.onNodeWithTag("fake_player_abc").assertIsDisplayed()
    }

    @Test
    fun aPlayerErrorOffersOpenInYouTube() {
        show(content(), player = PlayerUiState(videoKey = "abc", hasError = true))
        composeRule.onNodeWithText("Open in YouTube").assertIsDisplayed()
    }

    @Test
    fun reviewsTabShowsThreeReviewsAndSeeAll() {
        show(content())

        composeRule.onNodeWithTag(TestTags.DETAILS_CONTENT).performScrollToNode(hasText("See all reviews (18)"))
        composeRule.onNodeWithText("See all reviews (18)").performClick()

        assertEquals(1, seeAll)
    }

    @Test
    fun noReviewsShowsNoReviewsYet() {
        show(content(reviews = ReviewsPreviewState.Loaded(emptyList(), 0)))

        composeRule.onNodeWithTag(TestTags.DETAILS_CONTENT).performScrollToNode(hasText("No reviews yet"))
        composeRule.onNodeWithText("No reviews yet").assertIsDisplayed()
    }

    @Test
    fun trailersTabListsVideosAndPlaysTheTappedOne() {
        val clip = VideoUi("clip1", "Behind the curtain", VideoType.Clip, official = false, thumbnailUrl = null)
        show(content(videos = VideosState.Loaded(trailer, listOf(trailer, clip))), tab = DetailsTab.Trailers)

        composeRule.onNodeWithTag(TestTags.DETAILS_CONTENT).performScrollToNode(hasText("Behind the curtain"))
        composeRule.onNodeWithText("Behind the curtain").performClick()

        assertEquals("clip1", played)
    }

    @Test
    fun notFoundOffersBackInsteadOfRetry() {
        show(DetailsUiState.Error(AppError.NotFound()))

        composeRule.onNodeWithText("Movie not found").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertDoesNotExist()
        composeRule.onNodeWithText("Back").performClick()

        assertEquals(1, backs)
    }

    @Test
    fun parsingErrorRetries() {
        show(DetailsUiState.Error(AppError.Parsing))

        composeRule.onNodeWithText("Couldn't read the response").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(1, retries)
    }
}
