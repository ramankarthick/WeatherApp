package com.example.weather.service

import com.example.weather.data.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    fun getWeather(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"  // Default to Celsius or "imperial" for Fahrenheit
    ): Call<WeatherResponse>

    @GET("weather")
    fun getWeatherLatLon(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"  // Default to Celsius or "imperial" for Fahrenheit
    ): Call<WeatherResponse>

}