package com.yudistirosaputro.movieapi.ui.details

import androidx.compose.runtime.Immutable
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.MovieDetails
import com.yudistirosaputro.movieapi.domain.model.Video
import com.yudistirosaputro.movieapi.domain.model.VideoType
import com.yudistirosaputro.movieapi.ui.reviews.ReviewUi
import java.text.NumberFormat
import java.util.Locale

@Immutable
data class MovieDetailsUi(
    val id: Int,
    val title: String,
    val tagline: String?,
    val overview: String?,
    val backdropUrl: String?,
    val posterUrl: String?,
    val year: String?,
    val runtimeMinutes: Int?,
    val rating: String?,
    val voteCount: String?,
    val language: String?,
    val genres: String?,
)

@Immutable
data class VideoUi(
    val key: String,
    val title: String,
    val type: VideoType,
    val official: Boolean,
    val thumbnailUrl: String?,
)

sealed interface VideosState {
    data object Loading : VideosState
    data class Loaded(val trailer: VideoUi?, val videos: List<VideoUi>) : VideosState

    /** The videos call failed. Details stay usable; the trailer is simply reported as unavailable. */
    data object Unavailable : VideosState
}

sealed interface ReviewsPreviewState {
    data object Loading : ReviewsPreviewState
    data class Loaded(val reviews: List<ReviewUi>, val totalResults: Int) : ReviewsPreviewState
    data class Error(val error: AppError) : ReviewsPreviewState
}

sealed interface DetailsUiState {
    /** Built from the navigation arguments so the header (and the shared poster) render immediately. */
    data class Loading(val movieId: Int, val title: String?, val posterUrl: String?) : DetailsUiState
    data class Content(
        val details: MovieDetailsUi,
        val videos: VideosState,
        val reviews: ReviewsPreviewState,
    ) : DetailsUiState
    data class Error(val error: AppError) : DetailsUiState
}

@Immutable
data class PlayerUiState(
    val videoKey: String? = null,
    val isFullscreen: Boolean = false,
    val hasError: Boolean = false,
)

data class PlaybackStart(val seconds: Float, val autoPlay: Boolean)

enum class DetailsTab { Reviews, Trailers }

fun MovieDetails.toUi(locale: Locale = Locale.getDefault()): MovieDetailsUi = MovieDetailsUi(
    id = id,
    title = title,
    tagline = tagline,
    overview = overview,
    backdropUrl = backdropUrl,
    posterUrl = posterUrl,
    year = releaseDate?.year?.toString(),
    runtimeMinutes = runtimeMinutes,
    rating = if (voteCount > 0) String.format(locale, "%.1f", voteAverage) else null,
    voteCount = if (voteCount > 0) NumberFormat.getIntegerInstance(locale).format(voteCount) else null,
    language = originalLanguage?.uppercase(Locale.ROOT),
    genres = genres.joinToString(", ") { it.name }.ifBlank { null },
)

fun Video.toUi(): VideoUi = VideoUi(key = key, title = name, type = type, official = official, thumbnailUrl = thumbnailUrl)
