package com.yudistirosaputro.movieapi.domain.repository

import androidx.paging.PagingData
import com.yudistirosaputro.movieapi.domain.model.Genre
import com.yudistirosaputro.movieapi.domain.model.Movie
import com.yudistirosaputro.movieapi.domain.model.MovieDetails
import com.yudistirosaputro.movieapi.domain.model.Outcome
import com.yudistirosaputro.movieapi.domain.model.Paged
import com.yudistirosaputro.movieapi.domain.model.Review
import com.yudistirosaputro.movieapi.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getGenres(): Outcome<List<Genre>>

    /** [restoreIndex] is the first visible item to bring back after process death; pages up to it load up front. */
    fun discoverMovies(genreId: Int, restoreIndex: Int = 0): Flow<PagingData<Movie>>

    suspend fun getMovieDetails(movieId: Int): Outcome<MovieDetails>

    suspend fun getMovieVideos(movieId: Int): Outcome<List<Video>>

    suspend fun getReviewsPage(movieId: Int, page: Int = 1): Outcome<Paged<Review>>

    fun movieReviews(movieId: Int, restoreIndex: Int = 0): Flow<PagingData<Review>>
}
