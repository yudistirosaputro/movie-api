package com.yudistirosaputro.movieapi.ui.reviews

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.AppException
import com.yudistirosaputro.movieapi.ui.components.TestTags
import com.yudistirosaputro.movieapi.ui.theme.MovieApiTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val longText = (1..40).joinToString(" ") { "This sentence makes the review long enough to be collapsed number $it." }

    private fun review(id: String, content: String = "Short and sweet.", avatar: String? = null, rating: String? = "8.0") =
        ReviewUi(id = id, author = "Critic $id", initial = "C", avatarUrl = avatar, rating = rating, date = "Mar 3, 2024", content = content)

    private fun states(refresh: LoadState, append: LoadState = LoadState.NotLoading(true)) =
        LoadStates(refresh = refresh, prepend = LoadState.NotLoading(true), append = append)

    private fun show(data: PagingData<ReviewUi>) {
        composeRule.setContent {
            MovieApiTheme {
                ReviewsScreen(movieTitle = "Spider-Man", reviews = flowOf(data).collectAsLazyPagingItems(), onBack = {})
            }
        }
    }

    @Test
    fun readMoreExpandsALongReview() {
        composeRule.setContent { MovieApiTheme { ReviewItem(review("1", content = longText)) } }

        composeRule.onNodeWithText("Read more").performClick()

        composeRule.onNodeWithText("Show less").assertIsDisplayed()
    }

    @Test
    fun aShortReviewHasNoReadMore() {
        composeRule.setContent { MovieApiTheme { ReviewItem(review("1")) } }

        composeRule.onNodeWithText("Short and sweet.").assertIsDisplayed()
        composeRule.onNodeWithText("Read more").assertDoesNotExist()
    }

    @Test
    fun aReviewWithoutAvatarShowsTheInitialAndNoRatingChip() {
        composeRule.setContent { MovieApiTheme { ReviewItem(review("1", rating = null)) } }

        composeRule.onNodeWithContentDescription("Avatar of Critic 1").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Rated", substring = true).assertDoesNotExist()
    }

    @Test
    fun loadingShowsTheSkeleton() {
        show(PagingData.empty(states(LoadState.Loading, LoadState.NotLoading(false))))
        composeRule.onNodeWithTag(TestTags.SKELETON).assertIsDisplayed()
    }

    @Test
    fun contentListsReviews() {
        show(PagingData.from(listOf(review("1"), review("2")), states(LoadState.NotLoading(false))))

        composeRule.onNodeWithText("Critic 1").assertIsDisplayed()
        composeRule.onNodeWithText("Critic 2").assertIsDisplayed()
    }

    @Test
    fun noReviewsShowsTheEmptyState() {
        show(PagingData.empty(states(LoadState.NotLoading(false))))
        composeRule.onNodeWithText("No reviews yet").assertIsDisplayed()
    }

    @Test
    fun firstPageFailureShowsTheError() {
        show(PagingData.empty(states(LoadState.Error(AppException(AppError.Timeout)), LoadState.NotLoading(false))))

        composeRule.onNodeWithText("Connection timed out").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }
}
