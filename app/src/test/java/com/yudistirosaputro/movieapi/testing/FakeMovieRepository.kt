package com.yudistirosaputro.movieapi.testing

import androidx.paging.PagingData
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.Genre
import com.yudistirosaputro.movieapi.domain.model.Movie
import com.yudistirosaputro.movieapi.domain.model.MovieDetails
import com.yudistirosaputro.movieapi.domain.model.Outcome
import com.yudistirosaputro.movieapi.domain.model.Paged
import com.yudistirosaputro.movieapi.domain.model.Review
import com.yudistirosaputro.movieapi.domain.model.Video
import com.yudistirosaputro.movieapi.domain.model.VideoType
import com.yudistirosaputro.movieapi.domain.repository.MovieRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.LocalDate

class FakeMovieRepository : MovieRepository {
    var genres: Outcome<List<Genre>> = Outcome.Success(listOf(Genre(28, "Action"), Genre(35, "Comedy")))
    var details: Outcome<MovieDetails> = Outcome.Success(sampleDetails())
    var videos: Outcome<List<Video>> = Outcome.Success(listOf(sampleVideo("trailer", VideoType.Trailer), sampleVideo("clip", VideoType.Clip)))
    var reviews: Outcome<Paged<Review>> = Outcome.Success(Paged(listOf(sampleReview("1"), sampleReview("2"), sampleReview("3"), sampleReview("4")), 1, 2, 25))
    var movies: List<Movie> = listOf(Movie(1, "One", null, null, 2024, 7.0), Movie(2, "Two", null, null, null, 0.0))

    /** When set, getGenres suspends until the gate completes, so tests can observe Loading. */
    var genresGate: CompletableDeferred<Unit>? = null

    var genresCalls = 0
    var detailsCalls = 0
    var lastDiscoverRestoreIndex: Int? = null
    var lastReviewsRestoreIndex: Int? = null

    override suspend fun getGenres(): Outcome<List<Genre>> {
        genresCalls++
        genresGate?.await()
        return genres
    }

    override fun discoverMovies(genreId: Int, restoreIndex: Int): Flow<PagingData<Movie>> {
        lastDiscoverRestoreIndex = restoreIndex
        return flowOf(PagingData.from(movies))
    }

    override suspend fun getMovieDetails(movieId: Int): Outcome<MovieDetails> {
        detailsCalls++
        return details
    }

    override suspend fun getMovieVideos(movieId: Int): Outcome<List<Video>> = videos

    override suspend fun getReviewsPage(movieId: Int, page: Int): Outcome<Paged<Review>> = reviews

    override fun movieReviews(movieId: Int, restoreIndex: Int): Flow<PagingData<Review>> {
        lastReviewsRestoreIndex = restoreIndex
        return flowOf(PagingData.from(listOf(sampleReview("a"), sampleReview("b"))))
    }

    companion object {
        fun sampleDetails(
            runtime: Int? = 134,
            overview: String? = "A hero rises.",
            voteCount: Int = 1234,
        ) = MovieDetails(
            id = 969681,
            title = "Spider-Man: Brand New Day",
            tagline = "A brand new day starts now.",
            overview = overview,
            posterUrl = "https://image.tmdb.org/t/p/w342/p.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w1280/b.jpg",
            releaseDate = LocalDate.of(2026, 7, 31),
            runtimeMinutes = runtime,
            voteAverage = 8.25,
            voteCount = voteCount,
            genres = listOf(Genre(28, "Action"), Genre(12, "Adventure")),
            originalLanguage = "en",
        )

        fun sampleVideo(key: String, type: VideoType, official: Boolean = true) =
            Video(key, "Video $key", "YouTube", type, official, Instant.parse("2026-07-01T00:00:00Z"), "https://img.youtube.com/vi/$key/hqdefault.jpg")

        fun sampleReview(id: String) =
            Review(id, "Critic $id", null, 8.0, "Review $id", Instant.parse("2026-07-28T10:00:00Z"))

        val failure = Outcome.Failure(AppError.NoInternet)
    }
}
