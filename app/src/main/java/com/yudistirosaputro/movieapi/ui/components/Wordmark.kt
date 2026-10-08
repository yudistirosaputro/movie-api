package com.yudistirosaputro.movieapi.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.yudistirosaputro.movieapi.R
import com.yudistirosaputro.movieapi.ui.theme.Inter
import com.yudistirosaputro.movieapi.ui.theme.MovieColors

/** Original wordmark: lowercase "movie" in white, "api" in the accent, set tight in Inter Black. */
@Composable
fun Wordmark(modifier: Modifier = Modifier, fontSize: TextUnit = 26.sp) {
    val description = stringResource(R.string.cd_wordmark)
    val style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Black, fontSize = fontSize, letterSpacing = (-1.2).sp)
    Row(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = description
            heading()
        },
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(stringResource(R.string.wordmark_movie), style = style, color = MovieColors.TextPrimary)
        Text(stringResource(R.string.wordmark_api), style = style, color = MovieColors.Accent)
    }
}
