package com.yudistirosaputro.movieapi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yudistirosaputro.movieapi.R
import com.yudistirosaputro.movieapi.ui.common.FooterState
import com.yudistirosaputro.movieapi.ui.common.toErrorUi
import com.yudistirosaputro.movieapi.ui.theme.MovieColors

@Composable
fun PagingFooter(state: FooterState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    when (state) {
        FooterState.None -> Unit
        FooterState.Loading -> FooterLoader(modifier)
        is FooterState.Error -> Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .testTag(TestTags.FOOTER_ERROR),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.error_append_message),
                style = MaterialTheme.typography.titleMedium,
                color = MovieColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(state.error.toErrorUi().message),
                style = MaterialTheme.typography.bodySmall,
                color = MovieColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            PrimaryButton(text = stringResource(R.string.action_retry), onClick = onRetry, icon = Icons.Rounded.Refresh)
        }
    }
}
