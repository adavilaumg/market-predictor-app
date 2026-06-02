package com.example.marketpredictionapp.model

import com.google.gson.annotations.SerializedName

// ─── Auth ─────────────────────────────────────────────────────

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type")   val tokenType: String = "bearer"
)

// ─── Weather ──────────────────────────────────────────────────

data class WeatherResponse(
    val city:        String,
    val country:     String,
    val temperature: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    val humidity:    Int,
    val description: String,
    @SerializedName("wind_speed") val windSpeed: Double
)

// ─── Market ───────────────────────────────────────────────────

data class MarketRecord(
    val symbol:      String,
    @SerializedName("open_price")  val openPrice:  Double,
    @SerializedName("close_price") val closePrice: Double,
    @SerializedName("high_price")  val highPrice:  Double,
    @SerializedName("low_price")   val lowPrice:   Double,
    val volume:      Double?,
    val date:        String
)

data class EodResponse(
    val symbols: List<String>,
    val data:    List<MarketRecord>,
    val count:   Int
)

// ─── Analysis ─────────────────────────────────────────────────

data class AnalysisRequest(
    val city:    String,
    val symbols: List<String>
)

data class SymbolStats(
    val symbol:      String,
    @SerializedName("avg_price")   val avgPrice:   Double,
    @SerializedName("min_price")   val minPrice:   Double,
    @SerializedName("max_price")   val maxPrice:   Double,
    @SerializedName("data_points") val dataPoints: Int
)

data class AnalysisResponse(
    val city:               String,
    @SerializedName("avg_temperature") val avgTemperature: Double,
    @SerializedName("weather_records") val weatherRecords: Int,
    val symbols:            List<SymbolStats>,
    val message:            String
)

// ─── Correlations ─────────────────────────────────────────────

data class CorrelationRecord(
    val id:           String?,
    val city:         String,
    val symbol:       String,
    val temperature:  Double,
    @SerializedName("close_price") val closePrice: Double,
    @SerializedName("created_at")  val createdAt:  String?
)