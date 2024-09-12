package com.example.weather.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.data.WeatherResponse
import com.example.weather.service.GetWeatherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherViewModel: ViewModel() {

    private val apiKey = "91a6d6deb8b1483f279a5a6bf9b3ea02"
    private val weather = MutableLiveData<WeatherResponse>()
    val cityWeather: LiveData<WeatherResponse> get() = weather

    private val error = MutableLiveData<String>()
    val errorName: LiveData<String> get() = error

    // Get the Weather of a City Name, State or State Code and Country name
    // and update to LiveData
    fun getWeather(cityName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            GetWeatherService.weatherApi.getWeather(cityName, apiKey)
                .enqueue(object : Callback<WeatherResponse> {
                    override fun onResponse(
                        call: Call<WeatherResponse>,
                        response: Response<WeatherResponse>
                    ) {
                        if (response.isSuccessful) {
                            weather.value = response.body()
                        } else {
                            error.value = "Error: ${response.code()}"
                        }
                    }

                    override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                        error.value = t.message
                    }
                })
        }
    }

    //Get the Weather using Lat and Lon
    // and update to LiveData
    fun getWeatherUsingLatLon(lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            GetWeatherService.weatherApi.getWeatherLatLon(lat, lon, apiKey)
                .enqueue(object : Callback<WeatherResponse> {
                    override fun onResponse(
                        call: Call<WeatherResponse>,
                        response: Response<WeatherResponse>
                    ) {
                        if (response.isSuccessful) {
                            weather.value = response.body()
                        } else {
                            error.value = "Error: ${response.code()}"
                        }
                    }

                    override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                        error.value = t.message
                    }
                })
        }
    }
}