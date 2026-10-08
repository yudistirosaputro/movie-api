package com.yudistirosaputro.movieapi.domain.usecase

import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.Outcome
import com.yudistirosaputro.movieapi.domain.model.Paged
import com.yudistirosaputro.movieapi.domain.model.VideoType
import com.yudistirosaputro.movieapi.testing.FakeMovieRepository
import com.yudistirosaputro.movieapi.testing.FakeMovieRepository.Companion.sampleReview
import com.yudistirosaputro.movieapi.testing.FakeMovieRepository.Companion.sampleVideo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UseCasesTest {

    private val repository = FakeMovieRepository()

    @Test
    fun `videos use case selects the trailer and lists youtube videos with it first`() = runTest {
        repository.videos = Outcome.Success(listOf(sampleVideo("clip", VideoType.Clip), sampleVideo("trailer", VideoType.Trailer)))

        val result = (GetMovieVideosUseCase(repository)(1) as Outcome.Success).value

        assertEquals("trailer", result.trailer?.key)
        assertEquals(listOf("trailer", "clip"), result.youTubeVideos.map { it.key })
    }

    @Test
    fun `videos use case has no trailer when only clips exist`() = runTest {
        repository.videos = Outcome.Success(listOf(sampleVideo("clip", VideoType.Clip)))

        assertNull((GetMovieVideosUseCase(repository)(1) as Outcome.Success).value.trailer)
    }

    @Test
    fun `videos use case passes failures through`() = runTest {
        repository.videos = Outcome.Failure(AppError.Server(503))

        assertEquals(Outcome.Failure(AppError.Server(503)), GetMovieVideosUseCase(repository)(1))
    }

    @Test
    fun `reviews preview keeps three reviews and the total count`() = runTest {
        repository.reviews = Outcome.Success(Paged((1..20).map { sampleReview("$it") }, page = 1, totalPages = 3, totalResults = 47))

        val preview = (GetReviewsPreviewUseCase(repository)(1) as Outcome.Success).value

        assertEquals(listOf("1", "2", "3"), preview.reviews.map { it.id })
        assertEquals(47, preview.totalResults)
    }

    @Test
    fun `reviews preview of an empty page is empty`() = runTest {
        repository.reviews = Outcome.Success(Paged(emptyList(), page = 1, totalPages = 0, totalResults = 0))

        val preview = (GetReviewsPreviewUseCase(repository)(1) as Outcome.Success).value

        assertEquals(0, preview.totalResults)
        assertEquals(emptyList<Any>(), preview.reviews)
    }
}
