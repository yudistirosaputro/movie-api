package com.yudistirosaputro.movieapi.ui.reviews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.yudistirosaputro.movieapi.domain.usecase.GetMovieReviewsUseCase
import com.yudistirosaputro.movieapi.ui.navigation.ReviewsDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    getMovieReviews: GetMovieReviewsUseCase,
) : ViewModel() {

    private val destination = savedStateHandle.toRoute<ReviewsDestination>()

    val movieTitle: String? = destination.title

    val reviews: Flow<PagingData<ReviewUi>> =
        getMovieReviews(destination.movieId, restoreIndex = savedStateHandle[KEY_FIRST_VISIBLE] ?: 0)
            .map { page -> page.map { it.toUi() } }
            .cachedIn(viewModelScope)

    fun onFirstVisibleItemChanged(index: Int) {
        savedStateHandle[KEY_FIRST_VISIBLE] = index
    }

    private companion object {
        const val KEY_FIRST_VISIBLE = "first_visible_index"
    }
}
