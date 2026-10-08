package com.yudistirosaputro.movieapi.data.mapper

object TmdbImages {
    private const val BASE_URL = "https://image.tmdb.org/t/p/"
    const val POSTER_SIZE = "w342"
    const val BACKDROP_SIZE = "w1280"
    const val AVATAR_SIZE = "w185"

    fun poster(path: String?): String? = url(path, POSTER_SIZE)

    fun backdrop(path: String?): String? = url(path, BACKDROP_SIZE)

    /** TMDB sometimes stores a full Gravatar URL behind a leading slash, e.g. "/https://secure.gravatar.com/...". */
    fun avatar(path: String?): String? {
        val trimmed = path?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        val withoutSlash = trimmed.removePrefix("/")
        return when {
            withoutSlash.startsWith("https://") -> withoutSlash
            withoutSlash.startsWith("http://") -> "https://" + withoutSlash.removePrefix("http://")
            else -> url(trimmed, AVATAR_SIZE)
        }
    }

    fun youTubeThumbnail(key: String): String = "https://img.youtube.com/vi/$key/hqdefault.jpg"

    private fun url(path: String?, size: String): String? {
        val trimmed = path?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        return BASE_URL + size + if (trimmed.startsWith("/")) trimmed else "/$trimmed"
    }
}
