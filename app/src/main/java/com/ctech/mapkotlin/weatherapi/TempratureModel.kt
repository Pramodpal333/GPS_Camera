package com.ctech.mapkotlin.weatherapi

import com.google.gson.annotations.SerializedName

data class TempratureModel (
    @SerializedName("main") val main: MainModel
        )