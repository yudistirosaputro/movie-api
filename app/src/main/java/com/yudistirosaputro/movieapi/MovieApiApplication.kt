package com.yudistirosaputro.movieapi

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MovieApiApplication : Application(), SingletonImageLoader.Factory {

    /** Images use Coil's own OkHttp client: TMDB's image CDN needs no auth and shouldn't flood the API captures. */
    override fun newImageLoader(context: PlatformContext): ImageLoader = ImageLoader.Builder(context)
        .crossfade(true)
        .build()
}
