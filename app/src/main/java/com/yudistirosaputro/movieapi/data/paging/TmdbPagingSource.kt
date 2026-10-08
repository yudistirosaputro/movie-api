package com.yudistirosaputro.movieapi.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yudistirosaputro.movieapi.data.error.ErrorMapper
import com.yudistirosaputro.movieapi.data.remote.dto.PagedResponseDto
import com.yudistirosaputro.movieapi.domain.model.AppException
import kotlinx.coroutines.CancellationException

/**
 * Pages through any TMDB list endpoint. Keys are TMDB page numbers starting at 1.
 *
 * TMDB re-ranks popularity lists between requests, so the same id can show up on two pages;
 * those repeats are dropped here so LazyList keys stay unique.
 */
class TmdbPagingSource<Dto : Any, T : Any>(
    private val errorMapper: ErrorMapper,
    private val fetchPage: suspend (page: Int) -> PagedResponseDto<Dto>,
    private val mapItem: (Dto) -> T?,
    private val itemKey: (T) -> Any,
) : PagingSource<Int, T>() {

    private val seenKeys = mutableSetOf<Any>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val startPage = params.key ?: FIRST_PAGE
        val pagesToLoad = if (params is LoadParams.Refresh) (params.loadSize / PAGE_SIZE).coerceAtLeast(1) else 1
        return try {
            val items = ArrayList<T>()
            val newKeys = mutableSetOf<Any>()
            var page = startPage
            var reachedEnd: Boolean
            while (true) {
                val response = fetchPage(page)
                val lastPage = minOf(response.totalPages, MAX_PAGE)
                response.results.forEach { dto ->
                    val item = mapItem(dto) ?: return@forEach
                    val key = itemKey(item)
                    if (key !in seenKeys && newKeys.add(key)) items += item
                }
                reachedEnd = page >= lastPage || response.results.isEmpty()
                if (reachedEnd || page - startPage + 1 >= pagesToLoad) break
                page++
            }
            // Commit only on success, so a failed append that is retried doesn't filter its own items out.
            seenKeys.addAll(newKeys)
            LoadResult.Page(
                data = items,
                prevKey = null,
                nextKey = if (reachedEnd) null else page + 1,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(AppException(errorMapper.map(e)))
        }
    }

    /** Always refresh from page 1; TMDB pages aren't stable enough to resume mid-list. */
    override fun getRefreshKey(state: PagingState<Int, T>): Int? = null

    companion object {
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 20

        /** TMDB rejects page numbers above 500. */
        const val MAX_PAGE = 500

        /** Caps how many pages a restored list loads up front after process death. */
        const val MAX_RESTORE_PAGES = 10

        fun config(initialPages: Int = 1): PagingConfig = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = PAGE_SIZE / 2,
            initialLoadSize = PAGE_SIZE * initialPages.coerceIn(1, MAX_RESTORE_PAGES),
            enablePlaceholders = false,
        )

        /** Pages needed so the item at [anchorIndex] and the row after it are loaded. */
        fun pagesToRestore(anchorIndex: Int?): Int =
            if (anchorIndex == null || anchorIndex <= 0) 1 else (anchorIndex / PAGE_SIZE + 2).coerceAtMost(MAX_RESTORE_PAGES)
    }
}
