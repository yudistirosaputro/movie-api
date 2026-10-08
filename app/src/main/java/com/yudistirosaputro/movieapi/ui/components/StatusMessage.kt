package com.yudistirosaputro.movieapi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yudistirosaputro.movieapi.R
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.ui.common.toErrorUi
import com.yudistirosaputro.movieapi.ui.theme.MovieColors

/** Offline-screen style: centered icon, white title, grey message, white action. */
@Composable
fun StatusMessage(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag(TestTags.STATUS_MESSAGE),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, tint = MovieColors.TextSecondary, modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MovieColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MovieColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null || secondaryLabel != null) {
                Spacer(Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (secondaryLabel != null && onSecondary != null) {
                        SecondaryButton(text = secondaryLabel, onClick = onSecondary)
                    }
                    if (actionLabel != null && onAction != null) {
                        PrimaryButton(text = actionLabel, onClick = onAction, icon = actionIcon)
                    }
                }
            }
        }
    }
}

/** A NotFound error offers Back instead of Retry: retrying a missing movie can't succeed. */
@Composable
fun ErrorState(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val ui = error.toErrorUi()
    val isNotFound = error is AppError.NotFound
    StatusMessage(
        icon = ui.icon,
        title = stringResource(ui.title),
        message = stringResource(ui.message),
        modifier = modifier,
        actionLabel = if (isNotFound && onBack != null) stringResource(R.string.action_back) else stringResource(R.string.action_retry),
        actionIcon = if (isNotFound && onBack != null) null else Icons.Rounded.Refresh,
        onAction = if (isNotFound && onBack != null) onBack else onRetry,
        secondaryLabel = if (!isNotFound && onBack != null) stringResource(R.string.action_back) else null,
        onSecondary = if (!isNotFound) onBack else null,
    )
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    StatusMessage(
        icon = icon,
        title = title,
        message = message,
        modifier = modifier,
        actionLabel = if (onRetry != null) stringResource(R.string.action_retry) else null,
        actionIcon = Icons.Rounded.Refresh,
        onAction = onRetry,
    )
}
