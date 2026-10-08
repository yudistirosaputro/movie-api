package com.yudistirosaputro.movieapi.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MovieColorScheme = darkColorScheme(
    primary = MovieColors.Accent,
    onPrimary = MovieColors.TextPrimary,
    secondary = MovieColors.TextPrimary,
    onSecondary = MovieColors.Black,
    background = MovieColors.Background,
    onBackground = MovieColors.TextPrimary,
    surface = MovieColors.SurfaceLow,
    onSurface = MovieColors.TextPrimary,
    surfaceVariant = MovieColors.Surface,
    onSurfaceVariant = MovieColors.TextSecondary,
    surfaceContainer = MovieColors.Surface,
    surfaceContainerHigh = MovieColors.SurfaceHigh,
    outline = MovieColors.Outline,
    outlineVariant = MovieColors.Divider,
    error = MovieColors.Accent,
)

private val MovieShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
)

/** Dark only: the design has no light variant. */
@Composable
fun MovieApiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MovieColorScheme,
        typography = MovieTypography,
        shapes = MovieShapes,
        content = content,
    )
}
