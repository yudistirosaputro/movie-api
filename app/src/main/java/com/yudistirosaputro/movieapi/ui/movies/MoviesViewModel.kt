package com.yudistirosaputro.movieapi.ui.movies

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.yudistirosaputro.movieapi.domain.model.Movie
import com.yudistirosaputro.movieapi.domain.usecase.GetMoviesByGenreUseCase
import com.yudistirosaputro.movieapi.ui.navigation.MoviesDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@Immutable
data class MovieUi(val id: Int, val title: String, val posterUrl: String?)

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    getMoviesByGenre: GetMoviesByGenreUseCase,
) : ViewModel() {

    private val destination = savedStateHandle.toRoute<MoviesDestination>()

    val genreName: String = destination.genreName

    /** Survives rotation through cachedIn; survives process death by reloading up to the saved scroll index. */
    val movies: Flow<PagingData<MovieUi>> =
        getMoviesByGenre(destination.genreId, restoreIndex = savedStateHandle[KEY_FIRST_VISIBLE] ?: 0)
            .map { page -> page.map(Movie::toUi) }
            .cachedIn(viewModelScope)

    fun onFirstVisibleItemChanged(index: Int) {
        savedStateHandle[KEY_FIRST_VISIBLE] = index
    }

    private companion object {
        const val KEY_FIRST_VISIBLE = "first_visible_index"
    }
}

private fun Movie.toUi() = MovieUi(id = id, title = title, posterUrl = posterUrl)
