package com.yudistirosaputro.movieapi.ui.genres

import app.cash.turbine.test
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.Outcome
import com.yudistirosaputro.movieapi.domain.usecase.GetGenresUseCase
import com.yudistirosaputro.movieapi.testing.FakeMovieRepository
import com.yudistirosaputro.movieapi.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GenresViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMovieRepository()

    private fun viewModel() = GenresViewModel(GetGenresUseCase(repository))

    @Test
    fun `shows loading, then the genres`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.genresGate = gate
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(GenresUiState.Loading, awaitItem())
            gate.complete(Unit)
            assertEquals(GenresUiState.Content(listOf(GenreUi(28, "Action"), GenreUi(35, "Comedy"))), awaitItem())
        }
    }

    @Test
    fun `an empty list is the Empty state`() = runTest {
        repository.genres = Outcome.Success(emptyList())

        viewModel().uiState.test {
            assertEquals(GenresUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `a failure is the Error state`() = runTest {
        repository.genres = Outcome.Failure(AppError.Unauthorized("Invalid API key"))

        viewModel().uiState.test {
            assertEquals(GenresUiState.Error(AppError.Unauthorized("Invalid API key")), awaitItem())
        }
    }

    @Test
    fun `a missing api key surfaces as its own error`() = runTest {
        repository.genres = Outcome.Failure(AppError.MissingApiKey)

        viewModel().uiState.test {
            assertEquals(GenresUiState.Error(AppError.MissingApiKey), awaitItem())
        }
    }

    @Test
    fun `retry recovers after the network comes back`() = runTest {
        repository.genres = Outcome.Failure(AppError.NoInternet)
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(GenresUiState.Error(AppError.NoInternet), awaitItem())
            repository.genres = Outcome.Success(listOf(com.yudistirosaputro.movieapi.domain.model.Genre(28, "Action")))
            viewModel.retry()
            assertEquals(GenresUiState.Loading, awaitItem())
            assertEquals(GenresUiState.Content(listOf(GenreUi(28, "Action"))), awaitItem())
        }
        assertEquals(2, repository.genresCalls)
    }
}
