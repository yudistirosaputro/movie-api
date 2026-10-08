package com.yudistirosaputro.movieapi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import com.yudistirosaputro.movieapi.data.error.ErrorMapper
import com.yudistirosaputro.movieapi.data.remote.dto.PagedResponseDto
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.AppException
import com.yudistirosaputro.movieapi.testing.testJson
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class TmdbPagingSourceTest {

    private val requestedPages = mutableListOf<Int>()

    /** Pages of 20 ids: page n holds ids (n-1)*20+1 .. n*20, unless [pages] overrides it. */
    private fun source(
        totalPages: Int,
        failOn: Set<Int> = emptySet(),
        pages: Map<Int, List<Int>> = emptyMap(),
    ) = TmdbPagingSource<Int, Int>(
        errorMapper = ErrorMapper(testJson),
        fetchPage = { page ->
            requestedPages += page
            if (page in failOn) throw IOException("offline")
            val ids = pages[page] ?: ((page - 1) * 20 + 1..page * 20).toList()
            PagedResponseDto(page = page, results = ids, totalPages = totalPages, totalResults = totalPages * 20)
        },
        mapItem = { it },
        itemKey = { it },
    )

    private fun pager(source: TmdbPagingSource<Int, Int>, initialPages: Int = 1) =
        TestPager(TmdbPagingSource.config(initialPages), source)

    @Test
    fun `refresh loads page one with a next key`() = runTest {
        val page = pager(source(totalPages = 3)).refresh() as LoadResult.Page

        assertEquals((1..20).toList(), page.data)
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `append loads the following page`() = runTest {
        val pager = pager(source(totalPages = 3))
        pager.refresh()

        val page = pager.append() as LoadResult.Page

        assertEquals((21..40).toList(), page.data)
        assertEquals(3, page.nextKey)
        assertEquals(listOf(1, 2), requestedPages)
    }

    @Test
    fun `pagination ends at total_pages`() = runTest {
        val pager = pager(source(totalPages = 2))
        pager.refresh()

        val last = pager.append() as LoadResult.Page

        assertNull(last.nextKey)
    }

    @Test
    fun `pagination never goes beyond page 500 even when TMDB reports more`() = runTest {
        fun append(key: Int) = PagingSource.LoadParams.Append(key = key, loadSize = 20, placeholdersEnabled = false)

        val page499 = source(totalPages = 1000).load(append(499)) as LoadResult.Page
        val page500 = source(totalPages = 1000).load(append(500)) as LoadResult.Page

        assertEquals(500, page499.nextKey)
        assertNull(page500.nextKey)
    }

    @Test
    fun `an empty first page ends pagination`() = runTest {
        val page = pager(source(totalPages = 0, pages = mapOf(1 to emptyList()))).refresh() as LoadResult.Page

        assertTrue(page.data.isEmpty())
        assertNull(page.nextKey)
    }

    @Test
    fun `a failed refresh returns an AppException error`() = runTest {
        val result = pager(source(totalPages = 3, failOn = setOf(1))).refresh()

        val error = (result as LoadResult.Error).throwable
        assertEquals(AppError.NoInternet, (error as AppException).error)
    }

    @Test
    fun `a failed append keeps loaded pages and succeeds on retry`() = runTest {
        val failing = mutableSetOf(2)
        val source = TmdbPagingSource<Int, Int>(
            errorMapper = ErrorMapper(testJson),
            fetchPage = { page ->
                if (failing.remove(page)) throw IOException("offline")
                PagedResponseDto(page = page, results = ((page - 1) * 20 + 1..page * 20).toList(), totalPages = 3)
            },
            mapItem = { it },
            itemKey = { it },
        )
        val pager = pager(source)
        pager.refresh()

        assertTrue(pager.append() is LoadResult.Error)
        val retried = pager.append() as LoadResult.Page

        assertEquals((21..40).toList(), retried.data)
        assertEquals((1..40).toList(), pager.getPages().flatMap { it.data })
    }

    @Test
    fun `ids repeated on a later page are dropped`() = runTest {
        val pager = pager(source(totalPages = 2, pages = mapOf(1 to listOf(1, 2, 3), 2 to listOf(3, 4, 2, 5))))
        pager.refresh()

        val second = pager.append() as LoadResult.Page

        assertEquals(listOf(4, 5), second.data)
    }

    @Test
    fun `a restored refresh loads several pages at once`() = runTest {
        val page = pager(source(totalPages = 10), initialPages = 3).refresh() as LoadResult.Page

        assertEquals((1..60).toList(), page.data)
        assertEquals(4, page.nextKey)
        assertEquals(listOf(1, 2, 3), requestedPages)
    }

    @Test
    fun `pages to restore cover the saved index plus one page`() {
        assertEquals(1, TmdbPagingSource.pagesToRestore(null))
        assertEquals(1, TmdbPagingSource.pagesToRestore(0))
        assertEquals(2, TmdbPagingSource.pagesToRestore(5))
        assertEquals(4, TmdbPagingSource.pagesToRestore(45))
        assertEquals(TmdbPagingSource.MAX_RESTORE_PAGES, TmdbPagingSource.pagesToRestore(10_000))
    }
}
