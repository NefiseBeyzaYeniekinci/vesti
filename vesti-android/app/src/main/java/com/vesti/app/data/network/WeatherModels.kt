package com.vesti.app.data.network

data class WeatherResponse(
    val weather: List<WeatherDescription>,
    val main: WeatherMain
)

data class WeatherDescription(
    val main: String,
    val description: String,
    val icon: String
)

data class WeatherMain(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)
