package com.yudistirosaputro.movieapi.data.remote.auth

import java.io.IOException

/** toString never reveals the secret, so a credential can't leak through logs or crash reports. */
sealed interface TmdbCredential {
    data object Missing : TmdbCredential

    /** v3 API key, sent as the `api_key` query parameter. */
    class ApiKey(val value: String) : TmdbCredential {
        override fun toString() = "ApiKey(***)"
    }

    /** v4 Read Access Token (a JWT), sent as `Authorization: Bearer`. */
    class BearerToken(val value: String) : TmdbCredential {
        override fun toString() = "BearerToken(***)"
    }

    companion object {
        private const val JWT_PREFIX = "eyJ"

        fun from(raw: String?): TmdbCredential {
            val value = raw?.trim().orEmpty().removeSurrounding("\"").removeSurrounding("'").trim()
            return when {
                value.isEmpty() -> Missing
                value.startsWith(JWT_PREFIX) -> BearerToken(value)
                else -> ApiKey(value)
            }
        }
    }
}

/** An IOException so OkHttp delivers it to the caller instead of crashing its dispatcher thread. */
class MissingApiKeyException : IOException("MOVIE_API Gradle property is not configured")
