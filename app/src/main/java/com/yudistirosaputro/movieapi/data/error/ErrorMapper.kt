package com.yudistirosaputro.movieapi.data.error

import com.yudistirosaputro.movieapi.data.remote.auth.MissingApiKeyException
import com.yudistirosaputro.movieapi.data.remote.dto.TmdbErrorDto
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.AppException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class ErrorMapper @Inject constructor(private val json: Json) {

    fun map(throwable: Throwable): AppError = when (throwable) {
        is AppException -> throwable.error
        is MissingApiKeyException -> AppError.MissingApiKey
        is SocketTimeoutException -> AppError.Timeout
        // OkHttp's call timeout surfaces as a bare InterruptedIOException("timeout").
        is InterruptedIOException -> if (throwable.message?.contains("timeout", ignoreCase = true) == true) AppError.Timeout else AppError.NoInternet
        is UnknownHostException, is ConnectException, is NoRouteToHostException -> AppError.NoInternet
        is HttpException -> fromHttp(throwable.code(), throwable.response()?.errorBody()?.string())
        is SerializationException -> AppError.Parsing
        is IOException -> AppError.NoInternet
        else -> AppError.Unknown()
    }

    fun fromHttp(code: Int, errorBody: String?): AppError {
        val message = parseStatusMessage(errorBody)
        return when (code) {
            401 -> AppError.Unauthorized(message)
            404 -> AppError.NotFound(message)
            429 -> AppError.RateLimited(message)
            in 500..599 -> AppError.Server(code, message)
            else -> AppError.Unknown(code, message)
        }
    }

    private fun parseStatusMessage(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return try {
            json.decodeFromString<TmdbErrorDto>(body).statusMessage?.takeIf { it.isNotBlank() }
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
