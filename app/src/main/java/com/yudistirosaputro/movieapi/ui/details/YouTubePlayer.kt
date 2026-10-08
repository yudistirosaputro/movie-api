package com.yudistirosaputro.movieapi.ui.details

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.yudistirosaputro.movieapi.R
import com.yudistirosaputro.movieapi.ui.components.TestTags

/**
 * Inline YouTube player bound to the current lifecycle: it pauses when the screen stops and is
 * released when it leaves composition. Changing [videoKey] recreates the player.
 */
@Composable
fun YouTubeTrailerPlayer(
    videoKey: String,
    playbackStart: () -> PlaybackStart,
    onProgress: (Float) -> Unit,
    onPlayingChanged: (Boolean) -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentStart by rememberUpdatedState(playbackStart)
    val currentOnProgress by rememberUpdatedState(onProgress)
    val currentOnPlayingChanged by rememberUpdatedState(onPlayingChanged)
    val currentOnError by rememberUpdatedState(onError)

    key(videoKey) {
        AndroidView(
            modifier = modifier.testTag(TestTags.PLAYER),
            factory = { context ->
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.view_youtube_player, FrameLayout(context), false) as YouTubePlayerView
                lifecycleOwner.lifecycle.addObserver(view)
                val options = IFramePlayerOptions.Builder(context)
                    .controls(1)
                    .rel(0)
                    .ivLoadPolicy(3)
                    .fullscreen(0)
                    .build()
                view.initialize(
                    object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            val start = currentStart()
                            if (start.autoPlay) {
                                youTubePlayer.loadVideo(videoKey, start.seconds)
                            } else {
                                youTubePlayer.cueVideo(videoKey, start.seconds)
                            }
                        }

                        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                            currentOnProgress(second)
                        }

                        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                            when (state) {
                                PlayerConstants.PlayerState.PLAYING -> currentOnPlayingChanged(true)
                                // A pause caused by the screen stopping (rotation, backgrounding) is not the
                                // user's choice, so it must not stop playback from resuming afterwards.
                                PlayerConstants.PlayerState.PAUSED,
                                PlayerConstants.PlayerState.ENDED,
                                -> if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                                    currentOnPlayingChanged(false)
                                }
                                else -> Unit
                            }
                        }

                        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                            currentOnError()
                        }
                    },
                    true,
                    options,
                )
                view
            },
            onRelease = { view ->
                lifecycleOwner.lifecycle.removeObserver(view)
                view.release()
            },
        )
    }
}

fun Context.openYouTube(videoKey: String) {
    val intent = Intent(Intent.ACTION_VIEW, "https://www.youtube.com/watch?v=$videoKey".toUri())
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No app or browser can open the link; nothing more to fall back to.
    }
}
