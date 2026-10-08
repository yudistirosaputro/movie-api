package com.yudistirosaputro.movieapi.data.remote.auth

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val credential: TmdbCredential) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder().header("Accept", "application/json")
        when (credential) {
            TmdbCredential.Missing -> throw MissingApiKeyException()
            is TmdbCredential.ApiKey -> builder.url(
                request.url.newBuilder().setQueryParameter(API_KEY_PARAM, credential.value).build(),
            )
            is TmdbCredential.BearerToken -> builder.header("Authorization", "Bearer ${credential.value}")
        }
        return chain.proceed(builder.build())
    }

    companion object {
        const val API_KEY_PARAM = "api_key"
    }
}
