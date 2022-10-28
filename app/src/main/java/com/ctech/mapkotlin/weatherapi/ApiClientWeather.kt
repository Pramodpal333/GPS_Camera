package com.ctech.mapkotlin.weatherapi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClientWeather {
    private val BASE_UR = "https://api.openweathermap.org/data/"

    fun getSome(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_UR)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}