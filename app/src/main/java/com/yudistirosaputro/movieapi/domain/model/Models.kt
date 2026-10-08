package com.yudistirosaputro.movieapi.domain.model

import java.time.Instant
import java.time.LocalDate

data class Genre(val id: Int, val name: String)

data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseYear: Int?,
    val voteAverage: Double,
)

data class MovieDetails(
    val id: Int,
    val title: String,
    val tagline: String?,
    val overview: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: LocalDate?,
    val runtimeMinutes: Int?,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<Genre>,
    val originalLanguage: String?,
)

data class Review(
    val id: String,
    val author: String,
    val avatarUrl: String?,
    val rating: Double?,
    val content: String,
    val createdAt: Instant?,
)

enum class VideoType {
    Trailer, Teaser, Clip, Featurette, BehindTheScenes, Bloopers, Other;

    companion object {
        fun from(raw: String?): VideoType = when (raw?.trim()?.lowercase()) {
            "trailer" -> Trailer
            "teaser" -> Teaser
            "clip" -> Clip
            "featurette" -> Featurette
            "behind the scenes" -> BehindTheScenes
            "bloopers" -> Bloopers
            else -> Other
        }
    }
}

data class Video(
    val key: String,
    val name: String,
    val site: String,
    val type: VideoType,
    val official: Boolean,
    val publishedAt: Instant?,
    val thumbnailUrl: String?,
) {
    val isYouTube: Boolean get() = site.equals(SITE_YOUTUBE, ignoreCase = true)

    companion object {
        const val SITE_YOUTUBE = "YouTube"
    }
}

data class MovieVideos(val youTubeVideos: List<Video>, val trailer: Video?)

data class Paged<T>(val items: List<T>, val page: Int, val totalPages: Int, val totalResults: Int)

data class ReviewsPreview(val reviews: List<Review>, val totalResults: Int)
