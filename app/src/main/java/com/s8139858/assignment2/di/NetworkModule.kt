package com.s8139858.assignment2.di

import com.google.gson.Gson
import com.s8139858.assignment2.BuildConfig
import com.s8139858.assignment2.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Provides the networking stack (Gson, OkHttp, Retrofit, [ApiService]).
 *
 * Key Hilt annotations shown here:
 * - @Module + @InstallIn(SingletonComponent): this object teaches Hilt how to
 *   build dependencies that live as long as the whole application.
 * - @Provides: Hilt cannot construct OkHttp/Retrofit itself, so we write a
 *   factory function and Hilt calls it whenever something requests that type.
 * - @Singleton (singleton pattern): exactly one instance per app — every screen
 *   shares the same OkHttpClient connection pool and Retrofit instance.
 * - @Named: a qualifier that distinguishes two Strings (baseUrl vs campus).
 * Timeouts are generous because the free-tier Render server can take 30–60 s
 * to wake from a cold start. The JSON parser is Gson (via GsonConverterFactory);
 * Moshi would be the usual alternative — same role, different library.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 60L

    @Provides
    @Named("baseUrl")
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

    @Provides
    @Named("campus")
    fun provideCampus(): String = BuildConfig.CAMPUS

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            builder.addInterceptor(logging)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @Named("baseUrl") baseUrl: String,
        gson: Gson,
        okHttpClient: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}
