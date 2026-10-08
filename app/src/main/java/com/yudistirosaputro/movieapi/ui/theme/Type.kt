package com.yudistirosaputro.movieapi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yudistirosaputro.movieapi.R

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_black, FontWeight.Black),
)

private fun style(size: Int, weight: FontWeight, lineHeight: Int, letterSpacing: Double = 0.0) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

val MovieTypography = Typography(
    displayLarge = style(48, FontWeight.Black, 52, -1.0),
    displayMedium = style(40, FontWeight.Black, 44, -0.8),
    displaySmall = style(34, FontWeight.Bold, 40, -0.5),
    headlineLarge = style(30, FontWeight.Bold, 36, -0.5),
    headlineMedium = style(26, FontWeight.Bold, 32, -0.3),
    headlineSmall = style(22, FontWeight.Bold, 28, -0.2),
    titleLarge = style(20, FontWeight.Bold, 26),
    titleMedium = style(16, FontWeight.SemiBold, 22),
    titleSmall = style(14, FontWeight.SemiBold, 20),
    bodyLarge = style(16, FontWeight.Normal, 24),
    bodyMedium = style(14, FontWeight.Normal, 20),
    bodySmall = style(12, FontWeight.Normal, 16),
    labelLarge = style(15, FontWeight.Bold, 20),
    labelMedium = style(13, FontWeight.SemiBold, 16),
    labelSmall = style(11, FontWeight.SemiBold, 14, 0.4),
)
