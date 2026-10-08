package com.yudistirosaputro.movieapi.ui.genres

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.Genre
import com.yudistirosaputro.movieapi.domain.model.Outcome
import com.yudistirosaputro.movieapi.domain.usecase.GetGenresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class GenreUi(val id: Int, val name: String)

sealed interface GenresUiState {
    data object Loading : GenresUiState
    data class Content(val genres: List<GenreUi>) : GenresUiState
    data object Empty : GenresUiState
    data class Error(val error: AppError) : GenresUiState
}

@HiltViewModel
class GenresViewModel @Inject constructor(
    private val getGenres: GetGenresUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<GenresUiState>(GenresUiState.Loading)
    val uiState: StateFlow<GenresUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        loadJob?.cancel()
        _uiState.value = GenresUiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = when (val result = getGenres()) {
                is Outcome.Success ->
                    if (result.value.isEmpty()) GenresUiState.Empty else GenresUiState.Content(result.value.map(Genre::toUi))
                is Outcome.Failure -> GenresUiState.Error(result.error)
            }
        }
    }
}

private fun Genre.toUi() = GenreUi(id = id, name = name)
