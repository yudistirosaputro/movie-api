package com.yudistirosaputro.movieapi.ui.common

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.asAppError

/** The Loading / Content / Empty / Error state of a paged list, derived from Paging's load states. */
sealed interface ListState {
    data object Loading : ListState
    data object Content : ListState
    data object Empty : ListState
    data class Error(val error: AppError) : ListState

    companion object {
        /** Loaded items always win: a failed refresh or append never hides what is already on screen. */
        fun from(loadStates: CombinedLoadStates, itemCount: Int): ListState {
            val refresh = loadStates.refresh
            return when {
                itemCount > 0 -> Content
                refresh is LoadState.Error -> Error(refresh.error.asAppError())
                refresh is LoadState.NotLoading && loadStates.append.endOfPaginationReached -> Empty
                else -> Loading
            }
        }
    }
}

/** Footer below a paged list: a loader while appending, an inline retry when an append fails. */
sealed interface FooterState {
    data object None : FooterState
    data object Loading : FooterState
    data class Error(val error: AppError) : FooterState

    companion object {
        fun from(loadStates: CombinedLoadStates): FooterState = when (val append = loadStates.append) {
            is LoadState.Loading -> Loading
            is LoadState.Error -> Error(append.error.asAppError())
            is LoadState.NotLoading -> None
        }
    }
}
