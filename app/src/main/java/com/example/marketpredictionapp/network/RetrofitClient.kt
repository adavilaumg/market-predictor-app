package com.example.marketpredictionapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ⚠️ Cambia esta URL por la de tu backend en producción
    // private const val BASE_URL = "http://10.0.2.2:8000/"
    //private const val BASE_URL = "http://192.168.1.5:8000/"
    //private const val BASE_URL = "http://localhost:8000"
    private const val BASE_URL = "https://market-predictor-backend.onrender.com/"

    // 10.0.2.2 = localhost del emulador Android → tu máquina host

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /** Formatea el token JWT para el header Authorization */
    fun bearerToken(token: String) = "Bearer $token"
}