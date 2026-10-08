package com.yudistirosaputro.movieapi.ui.common

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.VpnKeyOff
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.ui.graphics.vector.ImageVector
import com.yudistirosaputro.movieapi.R
import com.yudistirosaputro.movieapi.domain.model.AppError

data class ErrorUi(val icon: ImageVector, @param:StringRes val title: Int, @param:StringRes val message: Int)

fun AppError.toErrorUi(): ErrorUi = when (this) {
    AppError.NoInternet -> ErrorUi(Icons.Rounded.WifiOff, R.string.error_no_internet_title, R.string.error_no_internet_message)
    AppError.Timeout -> ErrorUi(Icons.Rounded.HourglassEmpty, R.string.error_timeout_title, R.string.error_timeout_message)
    is AppError.Unauthorized -> ErrorUi(Icons.Rounded.VpnKey, R.string.error_unauthorized_title, R.string.error_unauthorized_message)
    is AppError.NotFound -> ErrorUi(Icons.Rounded.SearchOff, R.string.error_not_found_title, R.string.error_not_found_message)
    is AppError.RateLimited -> ErrorUi(Icons.Rounded.Speed, R.string.error_rate_limited_title, R.string.error_rate_limited_message)
    is AppError.Server -> ErrorUi(Icons.Rounded.CloudOff, R.string.error_server_title, R.string.error_server_message)
    AppError.Parsing -> ErrorUi(Icons.Rounded.Report, R.string.error_parsing_title, R.string.error_parsing_message)
    AppError.MissingApiKey -> ErrorUi(Icons.Rounded.VpnKeyOff, R.string.error_missing_key_title, R.string.error_missing_key_message)
    is AppError.Unknown -> ErrorUi(Icons.Rounded.ErrorOutline, R.string.error_unknown_title, R.string.error_unknown_message)
}
