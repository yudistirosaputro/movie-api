package com.yudistirosaputro.movieapi.domain.trailer

import com.yudistirosaputro.movieapi.domain.model.Video
import com.yudistirosaputro.movieapi.domain.model.VideoType

object TrailerSelector {

    private val preference: Comparator<Video> =
        compareByDescending<Video> { it.official }
            .thenByDescending { it.publishedAt?.toEpochMilli() ?: Long.MIN_VALUE }

    /** Official, newest YouTube trailer first; then the same rule for teasers; otherwise null. */
    fun select(videos: List<Video>): Video? {
        val playable = videos.filter { it.isYouTube && it.key.isNotBlank() }
        return playable.best(VideoType.Trailer) ?: playable.best(VideoType.Teaser)
    }

    /** Every playable YouTube video: the selected trailer first, then by type and recency. */
    fun orderForListing(videos: List<Video>, selected: Video?): List<Video> =
        videos.filter { it.isYouTube && it.key.isNotBlank() }
            .sortedWith(
                compareByDescending<Video> { it.key == selected?.key }
                    .thenBy { it.type.ordinal }
                    .then(preference),
            )

    private fun List<Video>.best(type: VideoType): Video? = filter { it.type == type }.sortedWith(preference).firstOrNull()
}
