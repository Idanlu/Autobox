package com.autobox.app.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private const val BASE_URL = "https://api.arboxapp.com/"

    private val headerInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("User-Agent", "Autobox-Android/1.0 (Linux; Android; Mobile)")
        val request = requestBuilder.build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            // Low latency tuning for HTTP/2 connection pooling
            .retryOnConnectionFailure(true)
            .build()
    }

    // High performance snipe client with minimal connection overhead
    val snipeHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    val arboxApiService: ArboxApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArboxApiService::class.java)
    }

    val snipeApiService: ArboxApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(snipeHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArboxApiService::class.java)
    }
}
