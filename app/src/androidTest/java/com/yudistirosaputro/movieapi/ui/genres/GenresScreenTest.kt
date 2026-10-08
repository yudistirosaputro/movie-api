package com.yudistirosaputro.movieapi.ui.genres

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.ui.components.TestTags
import com.yudistirosaputro.movieapi.ui.theme.MovieApiTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenresScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var clickedGenre: GenreUi? = null
    private var retries = 0
    private var aboutClicks = 0

    private fun show(state: GenresUiState) {
        composeRule.setContent {
            MovieApiTheme {
                GenresScreen(
                    uiState = state,
                    onGenreClick = { clickedGenre = it },
                    onAboutClick = { aboutClicks++ },
                    onRetry = { retries++ },
                )
            }
        }
    }

    @Test
    fun loadingShowsTheSkeleton() {
        show(GenresUiState.Loading)
        composeRule.onNodeWithTag(TestTags.SKELETON).assertIsDisplayed()
    }

    @Test
    fun contentListsGenresAndOpensTheTappedOne() {
        show(GenresUiState.Content(listOf(GenreUi(28, "Action"), GenreUi(35, "Comedy"))))

        composeRule.onNodeWithText("Action").assertIsDisplayed()
        composeRule.onNodeWithText("Comedy").performClick()

        assertEquals(GenreUi(35, "Comedy"), clickedGenre)
    }

    @Test
    fun emptyShowsItsOwnMessageAndRetries() {
        show(GenresUiState.Empty)

        composeRule.onNodeWithText("No categories yet").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(1, retries)
    }

    @Test
    fun errorShowsAHumanMessageAndRetryCallsBack() {
        show(GenresUiState.Error(AppError.NoInternet))

        composeRule.onNodeWithText("No internet connection").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(1, retries)
    }

    @Test
    fun missingApiKeyIsExplained() {
        show(GenresUiState.Error(AppError.MissingApiKey))
        composeRule.onNodeWithText("API key missing").assertIsDisplayed()
    }

    @Test
    fun invalidApiKeyIsExplained() {
        show(GenresUiState.Error(AppError.Unauthorized("Invalid API key")))
        composeRule.onNodeWithText("Invalid API key").assertIsDisplayed()
    }

    @Test
    fun aboutOpensFromTheTopBar() {
        show(GenresUiState.Content(listOf(GenreUi(28, "Action"))))

        composeRule.onNodeWithContentDescription("About Movie API").performClick()

        assertEquals(1, aboutClicks)
    }
}
