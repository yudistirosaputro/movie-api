package com.yudistirosaputro.movieapi.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.yudistirosaputro.movieapi.ui.theme.MovieColors

/** "2024 · 2h 14m · ★ 7.8" followed by small outlined badges. */
@Composable
fun MetadataRow(
    items: List<String>,
    modifier: Modifier = Modifier,
    badges: List<Badge> = emptyList(),
) {
    if (items.isEmpty() && badges.isEmpty()) return
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        if (items.isNotEmpty()) {
            Text(
                text = items.joinToString(separator = "  ·  "),
                style = MaterialTheme.typography.bodyMedium,
                color = MovieColors.TextSecondary,
            )
        }
        badges.forEach { OutlinedBadge(it) }
    }
}

data class Badge(val text: String, val contentDescription: String? = null)

@Composable
fun OutlinedBadge(badge: Badge, modifier: Modifier = Modifier) {
    Text(
        text = badge.text,
        style = MaterialTheme.typography.labelSmall,
        color = MovieColors.TextSecondary,
        modifier = modifier
            .border(width = 1.dp, color = MovieColors.Outline, shape = MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .then(
                if (badge.contentDescription != null) {
                    Modifier.semantics { contentDescription = badge.contentDescription }
                } else {
                    Modifier
                },
            ),
    )
}
