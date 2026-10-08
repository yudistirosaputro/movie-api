package com.yudistirosaputro.movieapi.di

import com.yudistirosaputro.dimock.okhttp.Dimock
import com.yudistirosaputro.movieapi.BuildConfig
import com.yudistirosaputro.movieapi.data.remote.TmdbService
import com.yudistirosaputro.movieapi.data.remote.auth.AuthInterceptor
import com.yudistirosaputro.movieapi.data.remote.auth.TmdbCredential
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideCredential(): TmdbCredential = TmdbCredential.from(BuildConfig.MOVIE_API)

    @Provides
    @Singleton
    fun provideOkHttpClient(credential: TmdbCredential): OkHttpClient = OkHttpClient.Builder()
        // dimock goes first so a mocked response never needs (or sees) the credential. Release uses the no-op artifact.
        .addInterceptor(Dimock.interceptor())
        .addInterceptor(AuthInterceptor(credential))
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                        redactQueryParams(AuthInterceptor.API_KEY_PARAM)
                    },
                )
            }
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.TMDB_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideTmdbService(retrofit: Retrofit): TmdbService = retrofit.create(TmdbService::class.java)
}
