package com.yudistirosaputro.movieapi.data.repository

import androidx.paging.Pager
import androidx.paging.PagingData
import com.yudistirosaputro.movieapi.data.error.ErrorMapper
import com.yudistirosaputro.movieapi.data.mapper.toDomain
import com.yudistirosaputro.movieapi.data.paging.TmdbPagingSource
import com.yudistirosaputro.movieapi.data.remote.TmdbService
import com.yudistirosaputro.movieapi.data.remote.dto.MovieDto
import com.yudistirosaputro.movieapi.data.remote.dto.ReviewDto
import com.yudistirosaputro.movieapi.di.IoDispatcher
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.Genre
import com.yudistirosaputro.movieapi.domain.model.Movie
import com.yudistirosaputro.movieapi.domain.model.MovieDetails
import com.yudistirosaputro.movieapi.domain.model.Outcome
import com.yudistirosaputro.movieapi.domain.model.Paged
import com.yudistirosaputro.movieapi.domain.model.Review
import com.yudistirosaputro.movieapi.domain.model.Video
import com.yudistirosaputro.movieapi.domain.repository.MovieRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val service: TmdbService,
    private val errorMapper: ErrorMapper,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MovieRepository {

    override suspend fun getGenres(): Outcome<List<Genre>> = call {
        service.getMovieGenres(TmdbService.LANGUAGE).genres.mapNotNull { it.toDomain() }
    }

    override fun discoverMovies(genreId: Int, restoreIndex: Int): Flow<PagingData<Movie>> =
        Pager(config = TmdbPagingSource.config(TmdbPagingSource.pagesToRestore(restoreIndex))) {
            TmdbPagingSource<MovieDto, Movie>(
                errorMapper = errorMapper,
                fetchPage = { page ->
                    service.discoverMovies(
                        genreId = genreId,
                        sortBy = TmdbService.SORT_POPULARITY_DESC,
                        includeAdult = false,
                        page = page,
                    )
                },
                mapItem = { it.toDomain() },
                itemKey = { it.id },
            )
        }.flow

    override suspend fun getMovieDetails(movieId: Int): Outcome<MovieDetails> = call {
        service.getMovieDetails(movieId).toDomain() ?: throw MalformedPayloadException()
    }

    override suspend fun getMovieVideos(movieId: Int): Outcome<List<Video>> = call {
        service.getMovieVideos(movieId).results.mapNotNull { it.toDomain() }
    }

    override suspend fun getReviewsPage(movieId: Int, page: Int): Outcome<Paged<Review>> = call {
        service.getMovieReviews(movieId, page).toDomain { it.toDomain() }
    }

    override fun movieReviews(movieId: Int, restoreIndex: Int): Flow<PagingData<Review>> =
        Pager(config = TmdbPagingSource.config(TmdbPagingSource.pagesToRestore(restoreIndex))) {
            TmdbPagingSource<ReviewDto, Review>(
                errorMapper = errorMapper,
                fetchPage = { page -> service.getMovieReviews(movieId, page) },
                mapItem = { it.toDomain() },
                itemKey = { it.id },
            )
        }.flow

    private suspend fun <T> call(block: suspend () -> T): Outcome<T> = withContext(ioDispatcher) {
        try {
            Outcome.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: MalformedPayloadException) {
            Outcome.Failure(AppError.Parsing)
        } catch (e: Exception) {
            Outcome.Failure(errorMapper.map(e))
        }
    }

    /** A 200 response whose body lacks the fields the screen can't work without (e.g. the movie id). */
    private class MalformedPayloadException : Exception()
}
