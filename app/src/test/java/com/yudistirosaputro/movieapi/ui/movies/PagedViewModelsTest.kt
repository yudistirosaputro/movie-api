package com.yudistirosaputro.movieapi.ui.movies

import androidx.lifecycle.SavedStateHandle
import androidx.paging.testing.asSnapshot
import com.yudistirosaputro.movieapi.domain.usecase.GetMovieReviewsUseCase
import com.yudistirosaputro.movieapi.domain.usecase.GetMoviesByGenreUseCase
import com.yudistirosaputro.movieapi.testing.FakeMovieRepository
import com.yudistirosaputro.movieapi.testing.MainDispatcherRule
import com.yudistirosaputro.movieapi.ui.reviews.ReviewsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Robolectric because SavedStateHandle.toRoute decodes navigation arguments through android.os.Bundle. */
@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class PagedViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMovieRepository()

    @Test
    fun `movies screen maps domain movies to ui models`() = runTest {
        val viewModel = MoviesViewModel(
            SavedStateHandle(mapOf("genreId" to 28, "genreName" to "Action")),
            GetMoviesByGenreUseCase(repository),
        )

        assertEquals("Action", viewModel.genreName)
        assertEquals(listOf(MovieUi(1, "One", null), MovieUi(2, "Two", null)), flowOf(viewModel.movies.first()).asSnapshot())
        assertEquals(0, repository.lastDiscoverRestoreIndex)
    }

    @Test
    fun `movies screen restores the saved scroll index after process death`() {
        val handle = SavedStateHandle(mapOf("genreId" to 28, "genreName" to "Action"))
        MoviesViewModel(handle, GetMoviesByGenreUseCase(repository)).onFirstVisibleItemChanged(57)

        MoviesViewModel(handle, GetMoviesByGenreUseCase(repository))

        assertEquals(57, repository.lastDiscoverRestoreIndex)
    }

    @Test
    fun `reviews screen exposes the title and restores its scroll index`() = runTest {
        val handle = SavedStateHandle(mapOf("movieId" to 969681, "title" to "Spider-Man"))
        val first = ReviewsViewModel(handle, GetMovieReviewsUseCase(repository))
        assertEquals("Spider-Man", first.movieTitle)
        assertEquals(listOf("a", "b"), flowOf(first.reviews.first()).asSnapshot().map { it.id })

        first.onFirstVisibleItemChanged(31)
        ReviewsViewModel(handle, GetMovieReviewsUseCase(repository))

        assertEquals(31, repository.lastReviewsRestoreIndex)
    }
}
