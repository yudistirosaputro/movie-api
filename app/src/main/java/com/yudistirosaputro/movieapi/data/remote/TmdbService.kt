package com.yudistirosaputro.movieapi.data.remote

import com.yudistirosaputro.movieapi.data.remote.dto.GenresResponseDto
import com.yudistirosaputro.movieapi.data.remote.dto.MovieDetailsDto
import com.yudistirosaputro.movieapi.data.remote.dto.MovieDto
import com.yudistirosaputro.movieapi.data.remote.dto.PagedResponseDto
import com.yudistirosaputro.movieapi.data.remote.dto.ReviewDto
import com.yudistirosaputro.movieapi.data.remote.dto.VideosResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbService {

    @GET("genre/movie/list")
    suspend fun getMovieGenres(@Query("language") language: String): GenresResponseDto

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("with_genres") genreId: Int,
        @Query("sort_by") sortBy: String,
        @Query("include_adult") includeAdult: Boolean,
        @Query("page") page: Int,
    ): PagedResponseDto<MovieDto>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(@Path("movie_id") movieId: Int): MovieDetailsDto

    @GET("movie/{movie_id}/reviews")
    suspend fun getMovieReviews(@Path("movie_id") movieId: Int, @Query("page") page: Int): PagedResponseDto<ReviewDto>

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(@Path("movie_id") movieId: Int): VideosResponseDto

    companion object {
        const val LANGUAGE = "en-US"
        const val SORT_POPULARITY_DESC = "popularity.desc"
    }
}
