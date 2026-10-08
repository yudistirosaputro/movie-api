package com.yudistirosaputro.movieapi.data.mapper

import com.yudistirosaputro.movieapi.data.remote.dto.GenreDto
import com.yudistirosaputro.movieapi.data.remote.dto.MovieDetailsDto
import com.yudistirosaputro.movieapi.data.remote.dto.MovieDto
import com.yudistirosaputro.movieapi.data.remote.dto.PagedResponseDto
import com.yudistirosaputro.movieapi.data.remote.dto.ReviewDto
import com.yudistirosaputro.movieapi.data.remote.dto.VideoDto
import com.yudistirosaputro.movieapi.domain.model.Genre
import com.yudistirosaputro.movieapi.domain.model.Movie
import com.yudistirosaputro.movieapi.domain.model.MovieDetails
import com.yudistirosaputro.movieapi.domain.model.Paged
import com.yudistirosaputro.movieapi.domain.model.Review
import com.yudistirosaputro.movieapi.domain.model.Video
import com.yudistirosaputro.movieapi.domain.model.VideoType
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

fun GenreDto.toDomain(): Genre? {
    val id = id ?: return null
    val name = name?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return Genre(id = id, name = name)
}

fun MovieDto.toDomain(): Movie? {
    val id = id ?: return null
    return Movie(
        id = id,
        title = title.cleaned() ?: originalTitle.cleaned().orEmpty(),
        posterUrl = TmdbImages.poster(posterPath),
        backdropUrl = TmdbImages.backdrop(backdropPath),
        releaseYear = parseDate(releaseDate)?.year,
        voteAverage = voteAverage ?: 0.0,
    )
}

fun MovieDetailsDto.toDomain(): MovieDetails? {
    val id = id ?: return null
    return MovieDetails(
        id = id,
        title = title.cleaned() ?: originalTitle.cleaned().orEmpty(),
        tagline = tagline.cleaned(),
        overview = overview.cleaned(),
        posterUrl = TmdbImages.poster(posterPath),
        backdropUrl = TmdbImages.backdrop(backdropPath),
        releaseDate = parseDate(releaseDate),
        runtimeMinutes = runtime?.takeIf { it > 0 },
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        genres = genres.mapNotNull { it.toDomain() },
        originalLanguage = originalLanguage.cleaned(),
    )
}

fun ReviewDto.toDomain(): Review? {
    val id = id.cleaned() ?: return null
    return Review(
        id = id,
        author = author.cleaned() ?: authorDetails?.name.cleaned() ?: authorDetails?.username.cleaned().orEmpty(),
        avatarUrl = TmdbImages.avatar(authorDetails?.avatarPath),
        rating = authorDetails?.rating,
        content = content?.trim().orEmpty(),
        createdAt = parseInstant(createdAt),
    )
}

fun VideoDto.toDomain(): Video? {
    val key = key.cleaned() ?: return null
    val site = site.cleaned().orEmpty()
    return Video(
        key = key,
        name = name.cleaned().orEmpty(),
        site = site,
        type = VideoType.from(type),
        official = official ?: false,
        publishedAt = parseInstant(publishedAt),
        thumbnailUrl = if (site.equals(Video.SITE_YOUTUBE, ignoreCase = true)) TmdbImages.youTubeThumbnail(key) else null,
    )
}

fun <D, T> PagedResponseDto<D>.toDomain(mapItem: (D) -> T?): Paged<T> = Paged(
    items = results.mapNotNull(mapItem),
    page = page,
    totalPages = totalPages,
    totalResults = totalResults,
)

private fun String?.cleaned(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

private fun parseDate(raw: String?): LocalDate? {
    val value = raw.cleaned() ?: return null
    return try {
        LocalDate.parse(value)
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun parseInstant(raw: String?): Instant? {
    val value = raw.cleaned() ?: return null
    return try {
        Instant.parse(value)
    } catch (_: DateTimeParseException) {
        null
    }
}
