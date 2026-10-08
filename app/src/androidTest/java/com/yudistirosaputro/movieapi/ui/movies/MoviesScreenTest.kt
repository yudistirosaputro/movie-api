package com.yudistirosaputro.movieapi.ui.movies

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
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
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoviesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var clicked: MovieUi? = null

    private val movies = (1..9).map { MovieUi(it, "Movie $it", posterUrl = null) }

    private fun show(data: PagingData<MovieUi>) {
        composeRule.setContent {
            MovieApiTheme {
                MoviesScreen(
                    genreName = "Action",
                    movies = flowOf(data).collectAsLazyPagingItems(),
                    onBack = {},
                    onMovieClick = { clicked = it },
                )
            }
        }
    }

    private fun states(refresh: LoadState, append: LoadState = LoadState.NotLoading(false)) =
        LoadStates(refresh = refresh, prepend = LoadState.NotLoading(true), append = append)

    @Test
    fun firstLoadShowsTheSkeleton() {
        show(PagingData.empty(states(LoadState.Loading)))
        composeRule.onNodeWithTag(TestTags.SKELETON).assertIsDisplayed()
        composeRule.onNodeWithText("Action").assertIsDisplayed()
    }

    @Test
    fun contentShowsPostersByTitleAndOpensTheTappedOne() {
        show(PagingData.from(movies, states(LoadState.NotLoading(false), LoadState.NotLoading(true))))

        composeRule.onNodeWithContentDescription("Movie 2").performClick()

        assertEquals(movies[1], clicked)
    }

    @Test
    fun emptyGenreShowsTheEmptyState() {
        show(PagingData.empty(states(LoadState.NotLoading(false), LoadState.NotLoading(true))))
        composeRule.onNodeWithText("No movies here yet").assertIsDisplayed()
    }

    @Test
    fun firstPageFailureIsFullScreen() {
        show(PagingData.empty(states(LoadState.Error(AppException(AppError.Server(503))))))

        composeRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun appendFailureKeepsLoadedItemsAndShowsAFooterRetry() {
        show(PagingData.from(movies, states(LoadState.NotLoading(false), LoadState.Error(AppException(AppError.Server(500))))))

        composeRule.onNodeWithContentDescription("Movie 1").assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.MOVIE_GRID).performScrollToNode(hasTestTag(TestTags.FOOTER_ERROR))
        composeRule.onNodeWithText("Couldn't load more").assertIsDisplayed()
    }

    @Test
    fun appendingShowsTheFooterLoader() {
        show(PagingData.from(movies, states(LoadState.NotLoading(false), LoadState.Loading)))

        composeRule.onNodeWithTag(TestTags.MOVIE_GRID).performScrollToNode(hasTestTag(TestTags.FOOTER_LOADER))
        composeRule.onNodeWithTag(TestTags.FOOTER_LOADER).assertIsDisplayed()
    }
}
