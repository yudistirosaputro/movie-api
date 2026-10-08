package com.yudistirosaputro.movieapi.domain.trailer

import com.yudistirosaputro.movieapi.domain.model.Video
import com.yudistirosaputro.movieapi.domain.model.VideoType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class TrailerSelectorTest {

    private fun video(
        key: String,
        type: VideoType = VideoType.Trailer,
        site: String = "YouTube",
        official: Boolean = true,
        published: String? = "2024-01-01T00:00:00Z",
    ) = Video(key, key, site, type, official, published?.let(Instant::parse), null)

    @Test
    fun `prefers an official trailer over a newer unofficial one`() {
        val videos = listOf(
            video("unofficial-new", official = false, published = "2024-06-01T00:00:00Z"),
            video("official-old", official = true, published = "2024-01-01T00:00:00Z"),
        )
        assertEquals("official-old", TrailerSelector.select(videos)?.key)
    }

    @Test
    fun `among official trailers picks the newest`() {
        val videos = listOf(
            video("old", published = "2023-01-01T00:00:00Z"),
            video("newest", published = "2024-05-01T00:00:00Z"),
            video("undated", published = null),
        )
        assertEquals("newest", TrailerSelector.select(videos)?.key)
    }

    @Test
    fun `ignores trailers that are not on YouTube`() {
        val videos = listOf(video("vimeo", site = "Vimeo"), video("teaser", type = VideoType.Teaser))
        assertEquals("teaser", TrailerSelector.select(videos)?.key)
    }

    @Test
    fun `falls back to the best teaser when there is no trailer`() {
        val videos = listOf(
            video("clip", type = VideoType.Clip),
            video("teaser-unofficial", type = VideoType.Teaser, official = false, published = "2024-09-01T00:00:00Z"),
            video("teaser-official", type = VideoType.Teaser, official = true, published = "2024-02-01T00:00:00Z"),
        )
        assertEquals("teaser-official", TrailerSelector.select(videos)?.key)
    }

    @Test
    fun `returns null when there is neither trailer nor teaser`() {
        assertNull(TrailerSelector.select(listOf(video("clip", type = VideoType.Clip), video("bts", type = VideoType.BehindTheScenes))))
        assertNull(TrailerSelector.select(emptyList()))
    }

    @Test
    fun `listing keeps every youtube video with the selected trailer first`() {
        val trailer = video("trailer")
        val videos = listOf(video("clip", type = VideoType.Clip), video("vimeo", site = "Vimeo"), trailer, video("teaser", type = VideoType.Teaser))
        val ordered = TrailerSelector.orderForListing(videos, trailer).map { it.key }
        assertEquals(listOf("trailer", "teaser", "clip"), ordered)
    }
}
