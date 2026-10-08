package com.yudistirosaputro.movieapi.data.mapper

import com.yudistirosaputro.movieapi.data.remote.dto.AuthorDetailsDto
import com.yudistirosaputro.movieapi.data.remote.dto.GenreDto
import com.yudistirosaputro.movieapi.data.remote.dto.MovieDetailsDto
import com.yudistirosaputro.movieapi.data.remote.dto.MovieDto
import com.yudistirosaputro.movieapi.data.remote.dto.ReviewDto
import com.yudistirosaputro.movieapi.data.remote.dto.VideoDto
import com.yudistirosaputro.movieapi.domain.model.Genre
import com.yudistirosaputro.movieapi.domain.model.VideoType
import com.yudistirosaputro.movieapi.testing.testJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class MappersTest {

    @Test
    fun `details with every optional field null degrade to fallbacks`() {
        val json = """
            {"id": 7, "title": "Nulls", "tagline": null, "overview": "", "poster_path": null, "backdrop_path": null,
             "release_date": "", "runtime": null, "vote_average": null, "vote_count": null, "genres": null,
             "original_language": null}
        """.trimIndent()
        val details = requireNotNull(testJson.decodeFromString<MovieDetailsDto>(json).toDomain())

        assertEquals("Nulls", details.title)
        assertNull(details.tagline)
        assertNull(details.overview)
        assertNull(details.posterUrl)
        assertNull(details.backdropUrl)
        assertNull(details.releaseDate)
        assertNull(details.runtimeMinutes)
        assertEquals(0.0, details.voteAverage, 0.0)
        assertEquals(0, details.voteCount)
        assertEquals(emptyList<Genre>(), details.genres)
        assertNull(details.originalLanguage)
    }

    @Test
    fun `runtime of zero is hidden`() {
        assertNull(MovieDetailsDto(id = 1, title = "x", runtime = 0).toDomain()?.runtimeMinutes)
    }

    @Test
    fun `full details map images, dates and genres`() {
        val details = requireNotNull(
            MovieDetailsDto(
                id = 1, title = "Title", posterPath = "/p.jpg", backdropPath = "/b.jpg", releaseDate = "2024-07-24",
                runtime = 134, voteAverage = 7.8, voteCount = 100,
                genres = listOf(GenreDto(28, "Action"), GenreDto(null, "Broken"), GenreDto(53, " ")),
            ).toDomain(),
        )
        assertEquals("https://image.tmdb.org/t/p/w342/p.jpg", details.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w1280/b.jpg", details.backdropUrl)
        assertEquals(LocalDate.of(2024, 7, 24), details.releaseDate)
        assertEquals(134, details.runtimeMinutes)
        assertEquals(listOf(Genre(28, "Action")), details.genres)
    }

    @Test
    fun `details without an id are rejected`() {
        assertNull(MovieDetailsDto(title = "no id").toDomain())
    }

    @Test
    fun `movie falls back to the original title and tolerates a bad date`() {
        val movie = requireNotNull(MovieDto(id = 3, title = " ", originalTitle = "Original", releaseDate = "soon").toDomain())
        assertEquals("Original", movie.title)
        assertNull(movie.releaseYear)
        assertNull(movie.posterUrl)
    }

    @Test
    fun `avatar paths handle tmdb images, gravatar urls behind a slash and null`() {
        assertEquals("https://image.tmdb.org/t/p/w185/a.jpg", TmdbImages.avatar("/a.jpg"))
        assertEquals("https://secure.gravatar.com/avatar/x.jpg", TmdbImages.avatar("/https://secure.gravatar.com/avatar/x.jpg"))
        assertEquals("https://www.gravatar.com/avatar/y.jpg", TmdbImages.avatar("/http://www.gravatar.com/avatar/y.jpg"))
        assertNull(TmdbImages.avatar(null))
        assertNull(TmdbImages.avatar(""))
    }

    @Test
    fun `review falls back to author details and keeps a null avatar and rating`() {
        val review = requireNotNull(
            ReviewDto(
                id = "r1", author = null, content = "  Great  ", createdAt = "2023-04-01T10:00:00.123Z",
                authorDetails = AuthorDetailsDto(name = "", username = "critic", avatarPath = null, rating = null),
            ).toDomain(),
        )
        assertEquals("critic", review.author)
        assertNull(review.avatarUrl)
        assertNull(review.rating)
        assertEquals("Great", review.content)
        assertEquals(Instant.parse("2023-04-01T10:00:00.123Z"), review.createdAt)
    }

    @Test
    fun `review without an id is dropped`() {
        assertNull(ReviewDto(id = null, author = "a").toDomain())
    }

    @Test
    fun `video maps type, official flag and youtube thumbnail`() {
        val video = requireNotNull(
            VideoDto(key = "abc", name = "Trailer", site = "YouTube", type = "Trailer", official = null, publishedAt = "2024-01-01T00:00:00.000Z").toDomain(),
        )
        assertEquals(VideoType.Trailer, video.type)
        assertEquals(false, video.official)
        assertEquals("https://img.youtube.com/vi/abc/hqdefault.jpg", video.thumbnailUrl)
    }

    @Test
    fun `non youtube video has no thumbnail and videos without a key are dropped`() {
        assertNull(VideoDto(key = "v1", site = "Vimeo", type = "Trailer").toDomain()?.thumbnailUrl)
        assertNull(VideoDto(key = " ", site = "YouTube").toDomain())
    }
}
