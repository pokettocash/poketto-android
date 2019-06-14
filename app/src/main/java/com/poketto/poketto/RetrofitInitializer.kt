package com.poketto.poketto

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInitializer {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://blockscout.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun explorer(): Explorer {
        return retrofit.create(Explorer::class.java)
    }
}