package com.yudistirosaputro.movieapi.domain.usecase

import androidx.paging.PagingData
import com.yudistirosaputro.movieapi.domain.model.Genre
import com.yudistirosaputro.movieapi.domain.model.Movie
import com.yudistirosaputro.movieapi.domain.model.MovieDetails
import com.yudistirosaputro.movieapi.domain.model.MovieVideos
import com.yudistirosaputro.movieapi.domain.model.Outcome
import com.yudistirosaputro.movieapi.domain.model.Review
import com.yudistirosaputro.movieapi.domain.model.ReviewsPreview
import com.yudistirosaputro.movieapi.domain.model.map
import com.yudistirosaputro.movieapi.domain.repository.MovieRepository
import com.yudistirosaputro.movieapi.domain.trailer.TrailerSelector
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGenresUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(): Outcome<List<Genre>> = repository.getGenres()
}

class GetMoviesByGenreUseCase @Inject constructor(private val repository: MovieRepository) {
    operator fun invoke(genreId: Int, restoreIndex: Int = 0): Flow<PagingData<Movie>> =
        repository.discoverMovies(genreId, restoreIndex)
}

class GetMovieDetailsUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(movieId: Int): Outcome<MovieDetails> = repository.getMovieDetails(movieId)
}

class GetMovieVideosUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(movieId: Int): Outcome<MovieVideos> = repository.getMovieVideos(movieId).map { videos ->
        val trailer = TrailerSelector.select(videos)
        MovieVideos(youTubeVideos = TrailerSelector.orderForListing(videos, trailer), trailer = trailer)
    }
}

class GetReviewsPreviewUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(movieId: Int): Outcome<ReviewsPreview> = repository.getReviewsPage(movieId, page = 1).map { page ->
        ReviewsPreview(reviews = page.items.take(PREVIEW_SIZE), totalResults = maxOf(page.totalResults, page.items.size))
    }

    companion object {
        const val PREVIEW_SIZE = 3
    }
}

class GetMovieReviewsUseCase @Inject constructor(private val repository: MovieRepository) {
    operator fun invoke(movieId: Int, restoreIndex: Int = 0): Flow<PagingData<Review>> =
        repository.movieReviews(movieId, restoreIndex)
}
