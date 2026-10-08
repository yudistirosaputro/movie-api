package com.yudistirosaputro.movieapi.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object GenresDestination

@Serializable
data class MoviesDestination(val genreId: Int, val genreName: String)

/** [title] and [posterUrl] let the details screen paint its header before the details request returns. */
@Serializable
data class DetailsDestination(val movieId: Int, val title: String? = null, val posterUrl: String? = null)

@Serializable
data class ReviewsDestination(val movieId: Int, val title: String? = null)

@Serializable
data object AboutDestination
