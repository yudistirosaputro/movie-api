package com.yudistirosaputro.movieapi.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yudistirosaputro.movieapi.R
import com.yudistirosaputro.movieapi.ui.theme.MovieColors

@Composable
fun RedLoader(modifier: Modifier = Modifier, size: Dp = 28.dp) {
    val description = stringResource(R.string.loading)
    CircularProgressIndicator(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = description },
        color = MovieColors.Accent,
        trackColor = Color.Transparent,
        strokeWidth = 3.dp,
    )
}

@Composable
fun FooterLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
            .testTag(TestTags.FOOTER_LOADER),
        contentAlignment = Alignment.Center,
    ) {
        RedLoader()
    }
}
