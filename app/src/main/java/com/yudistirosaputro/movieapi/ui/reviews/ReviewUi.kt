package com.yudistirosaputro.movieapi.ui.reviews

import androidx.compose.runtime.Immutable
import com.yudistirosaputro.movieapi.domain.model.Review
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Immutable
data class ReviewUi(
    val id: String,
    val author: String,
    val initial: String,
    val avatarUrl: String?,
    val rating: String?,
    val date: String?,
    val content: String,
)

fun Review.toUi(locale: Locale = Locale.getDefault(), zone: ZoneId = ZoneId.systemDefault()): ReviewUi {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).withZone(zone)
    return ReviewUi(
        id = id,
        author = author,
        initial = author.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "?",
        avatarUrl = avatarUrl,
        rating = rating?.let { String.format(locale, "%.1f", it) },
        date = createdAt?.let(formatter::format),
        content = content.replace("\r\n", "\n").trim(),
    )
}
