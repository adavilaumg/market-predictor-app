package com.example.marketpredictionapp.network

import com.example.marketpredictionapp.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ─────────────────────────────────────────────────

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    // ─── Weather ──────────────────────────────────────────────

    @GET("weather/current")
    suspend fun getCurrentWeather(
        @Header("Authorization") token: String,
        @Query("city")    city:    String,
        @Query("country") country: String = "GT"
    ): Response<WeatherResponse>

    @GET("weather/forecast")
    suspend fun getWeatherForecast(
        @Header("Authorization") token: String,
        @Query("city")    city:    String,
        @Query("country") country: String = "GT",
        @Query("days")    days:    Int    = 5
    ): Response<Map<String, Any>>

    // ─── Market ───────────────────────────────────────────────

    @GET("market/eod")
    suspend fun getEodPrices(
        @Header("Authorization") token: String,
        @Query("symbols") symbols: String,
        @Query("limit")   limit:   Int = 10
    ): Response<EodResponse>

    @GET("market/search")
    suspend fun searchTickers(
        @Header("Authorization") token: String,
        @Query("query") query: String
    ): Response<Map<String, Any>>

    // ─── Analysis ─────────────────────────────────────────────

    @POST("analysis")
    suspend fun runAnalysis(
        @Header("Authorization") token: String,
        @Body request: AnalysisRequest
    ): Response<AnalysisResponse>

    // ─── Correlations ─────────────────────────────────────────

    @GET("data/correlations")
    suspend fun getCorrelations(
        @Header("Authorization") token: String,
        @Query("city")   city:   String? = null,
        @Query("symbol") symbol: String? = null,
        @Query("limit")  limit:  Int     = 50
    ): Response<List<CorrelationRecord>>
}