package com.yudistirosaputro.movieapi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenresResponseDto(
    val genres: List<GenreDto> = emptyList(),
)

@Serializable
data class GenreDto(
    val id: Int? = null,
    val name: String? = null,
)

@Serializable
data class PagedResponseDto<T>(
    val page: Int = 1,
    val results: List<T> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 0,
    @SerialName("total_results") val totalResults: Int = 0,
)

@Serializable
data class MovieDto(
    val id: Int? = null,
    val title: String? = null,
    @SerialName("original_title") val originalTitle: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
)

@Serializable
data class MovieDetailsDto(
    val id: Int? = null,
    val title: String? = null,
    @SerialName("original_title") val originalTitle: String? = null,
    val tagline: String? = null,
    val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val runtime: Int? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("vote_count") val voteCount: Int? = null,
    val genres: List<GenreDto> = emptyList(),
    @SerialName("original_language") val originalLanguage: String? = null,
)

@Serializable
data class ReviewDto(
    val id: String? = null,
    val author: String? = null,
    @SerialName("author_details") val authorDetails: AuthorDetailsDto? = null,
    val content: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class AuthorDetailsDto(
    val name: String? = null,
    val username: String? = null,
    @SerialName("avatar_path") val avatarPath: String? = null,
    val rating: Double? = null,
)

@Serializable
data class VideosResponseDto(
    val id: Int? = null,
    val results: List<VideoDto> = emptyList(),
)

@Serializable
data class VideoDto(
    val key: String? = null,
    val name: String? = null,
    val site: String? = null,
    val type: String? = null,
    val official: Boolean? = null,
    @SerialName("published_at") val publishedAt: String? = null,
)

@Serializable
data class TmdbErrorDto(
    val success: Boolean? = null,
    @SerialName("status_code") val statusCode: Int? = null,
    @SerialName("status_message") val statusMessage: String? = null,
)
