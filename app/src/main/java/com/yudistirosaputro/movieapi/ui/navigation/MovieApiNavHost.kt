package com.yudistirosaputro.movieapi.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yudistirosaputro.movieapi.ui.about.AboutScreen
import com.yudistirosaputro.movieapi.ui.details.DetailsRoute
import com.yudistirosaputro.movieapi.ui.genres.GenresRoute
import com.yudistirosaputro.movieapi.ui.movies.MoviesRoute
import com.yudistirosaputro.movieapi.ui.reviews.ReviewsRoute

private const val TRANSITION_MS = 320

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MovieApiNavHost(navController: NavHostController = rememberNavController()) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = GenresDestination,
                enterTransition = { fadeIn(tween(TRANSITION_MS)) },
                exitTransition = { fadeOut(tween(TRANSITION_MS)) },
                popEnterTransition = { fadeIn(tween(TRANSITION_MS)) },
                popExitTransition = { fadeOut(tween(TRANSITION_MS)) },
            ) {
                composable<GenresDestination> { entry ->
                    WithAnimatedScope {
                        GenresRoute(
                            onGenreClick = { genre -> entry.ifResumed { navController.navigate(MoviesDestination(genre.id, genre.name)) } },
                            onAboutClick = { entry.ifResumed { navController.navigate(AboutDestination) } },
                        )
                    }
                }
                composable<MoviesDestination> { entry ->
                    WithAnimatedScope {
                        MoviesRoute(
                            onBack = { entry.ifResumed { navController.popBackStack() } },
                            onMovieClick = { movie ->
                                entry.ifResumed { navController.navigate(DetailsDestination(movie.id, movie.title, movie.posterUrl)) }
                            },
                        )
                    }
                }
                composable<DetailsDestination> { entry ->
                    WithAnimatedScope {
                        DetailsRoute(
                            onBack = { entry.ifResumed { navController.popBackStack() } },
                            onSeeAllReviews = { movieId, title ->
                                entry.ifResumed { navController.navigate(ReviewsDestination(movieId, title)) }
                            },
                        )
                    }
                }
                composable<ReviewsDestination> { entry ->
                    WithAnimatedScope { ReviewsRoute(onBack = { entry.ifResumed { navController.popBackStack() } }) }
                }
                composable<AboutDestination> { entry ->
                    WithAnimatedScope { AboutScreen(onBack = { entry.ifResumed { navController.popBackStack() } }) }
                }
            }
        }
    }
}

/** Drops clicks that land while a transition is running, so a double tap can't navigate or pop twice. */
private inline fun NavBackStackEntry.ifResumed(action: () -> Unit) {
    if (lifecycle.currentState == Lifecycle.State.RESUMED) action()
}

@Composable
private fun AnimatedContentScope.WithAnimatedScope(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this, content = content)
}
