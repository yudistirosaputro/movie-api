package com.yudistirosaputro.movieapi.ui.common

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.AppException
import com.yudistirosaputro.movieapi.domain.model.Review
import com.yudistirosaputro.movieapi.testing.FakeMovieRepository
import com.yudistirosaputro.movieapi.ui.details.toUi
import com.yudistirosaputro.movieapi.ui.reviews.toUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

class UiMappingTest {

    private fun states(refresh: LoadState, append: LoadState = LoadState.NotLoading(false)): CombinedLoadStates {
        val source = LoadStates(refresh = refresh, prepend = LoadState.NotLoading(true), append = append)
        return CombinedLoadStates(refresh, source.prepend, append, source, null)
    }

    private val offline = LoadState.Error(AppException(AppError.NoInternet))

    @Test
    fun `list state follows the first page`() {
        assertEquals(ListState.Loading, ListState.from(states(LoadState.Loading), itemCount = 0))
        assertEquals(ListState.Loading, ListState.from(states(LoadState.NotLoading(false)), itemCount = 0))
        assertEquals(ListState.Error(AppError.NoInternet), ListState.from(states(offline), itemCount = 0))
        assertEquals(ListState.Empty, ListState.from(states(LoadState.NotLoading(false), LoadState.NotLoading(true)), itemCount = 0))
        assertEquals(ListState.Content, ListState.from(states(LoadState.NotLoading(false)), itemCount = 20))
    }

    @Test
    fun `loaded items win over a later refresh or append failure`() {
        assertEquals(ListState.Content, ListState.from(states(offline), itemCount = 20))
        assertEquals(ListState.Content, ListState.from(states(LoadState.NotLoading(false), offline), itemCount = 20))
    }

    @Test
    fun `footer shows a loader while appending and an error when an append fails`() {
        assertEquals(FooterState.Loading, FooterState.from(states(LoadState.NotLoading(false), LoadState.Loading)))
        assertEquals(FooterState.Error(AppError.NoInternet), FooterState.from(states(LoadState.NotLoading(false), offline)))
        assertEquals(FooterState.None, FooterState.from(states(LoadState.NotLoading(false), LoadState.NotLoading(true))))
    }

    @Test
    fun `review ui formats the date, rating and initial`() {
        val ui = Review("1", "maria", null, 7.0, "Line one\r\nLine two", Instant.parse("2024-03-03T10:00:00Z"))
            .toUi(Locale.US, ZoneOffset.UTC)

        assertEquals("M", ui.initial)
        assertEquals("7.0", ui.rating)
        assertEquals("Mar 3, 2024", ui.date)
        assertEquals("Line one\nLine two", ui.content)
    }

    @Test
    fun `review ui without author, rating or date falls back`() {
        val ui = Review("1", "", null, null, "", null).toUi(Locale.US, ZoneOffset.UTC)

        assertEquals("?", ui.initial)
        assertNull(ui.rating)
        assertNull(ui.date)
    }

    @Test
    fun `details ui formats rating and vote count`() {
        val ui = FakeMovieRepository.sampleDetails().toUi(Locale.US)

        assertEquals("8.3", ui.rating)
        assertEquals("1,234", ui.voteCount)
        assertEquals(134, ui.runtimeMinutes)
    }
}
