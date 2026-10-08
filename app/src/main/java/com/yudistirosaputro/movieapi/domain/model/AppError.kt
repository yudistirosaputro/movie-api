package com.yudistirosaputro.movieapi.domain.model

sealed interface AppError {
    data object NoInternet : AppError
    data object Timeout : AppError
    data class Unauthorized(val serverMessage: String? = null) : AppError
    data class NotFound(val serverMessage: String? = null) : AppError
    data class RateLimited(val serverMessage: String? = null) : AppError
    data class Server(val code: Int, val serverMessage: String? = null) : AppError
    data object Parsing : AppError
    data object MissingApiKey : AppError
    data class Unknown(val code: Int? = null, val serverMessage: String? = null) : AppError
}

/** Carries an [AppError] through APIs that only accept a Throwable, such as Paging's LoadResult.Error. */
class AppException(val error: AppError) : Exception(error.toString())

fun Throwable.asAppError(): AppError = (this as? AppException)?.error ?: AppError.Unknown()
