package com.ctech.mapkotlin.weatherapi

import retrofit2.Call
import retrofit2.http.Query

interface GetTTempInterface {

    fun getCurrentWeatherData(
        @Query("lat") latitude : String,
        @Query("lon") longitude : String,
        @Query("APPID") api_key : String
    ): Call<MainModel>
}