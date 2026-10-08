package com.yudistirosaputro.movieapi.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yudistirosaputro.movieapi.BuildConfig
import com.yudistirosaputro.movieapi.R
import com.yudistirosaputro.movieapi.ui.components.BackButton
import com.yudistirosaputro.movieapi.ui.components.MovieTopBar
import com.yudistirosaputro.movieapi.ui.components.SecondaryButton
import com.yudistirosaputro.movieapi.ui.components.TopBarHeight
import com.yudistirosaputro.movieapi.ui.components.Wordmark
import com.yudistirosaputro.movieapi.ui.theme.MovieColors

private const val TMDB_URL = "https://www.themoviedb.org/"

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    versionName: String = BuildConfig.VERSION_NAME,
    versionCode: Int = BuildConfig.VERSION_CODE,
) {
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val barAlpha: State<Float> = remember(scrollState) { derivedStateOf { (scrollState.value / 240f).coerceIn(0f, 1f) } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MovieColors.Background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = topInset + TopBarHeight, bottom = bottomInset + 32.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(Modifier.height(24.dp))
                Wordmark(fontSize = MaterialTheme.typography.displayMedium.fontSize)
                Text(
                    text = stringResource(R.string.about_version, versionName, versionCode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MovieColors.TextSecondary,
                )
                Text(
                    text = stringResource(R.string.about_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MovieColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MovieColors.Divider)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.about_tmdb_attribution),
                    style = MaterialTheme.typography.titleMedium,
                    color = MovieColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.about_data_source),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MovieColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
                SecondaryButton(
                    text = stringResource(R.string.about_tmdb_link),
                    onClick = { uriHandler.openUri(TMDB_URL) },
                    icon = Icons.AutoMirrored.Rounded.OpenInNew,
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MovieColors.Divider)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.about_credits_heading),
                    style = MaterialTheme.typography.titleMedium,
                    color = MovieColors.TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.about_credit_font),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MovieColors.TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.about_credit_player),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MovieColors.TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        MovieTopBar(
            backgroundAlpha = barAlpha,
            navigationIcon = { BackButton(onClick = onBack) },
            title = {
                Text(
                    text = stringResource(R.string.about_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MovieColors.TextPrimary,
                    modifier = Modifier.semantics { heading() },
                )
            },
        )
    }
}
