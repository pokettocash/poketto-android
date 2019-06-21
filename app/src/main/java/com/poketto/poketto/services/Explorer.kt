package com.poketto.poketto.services

import com.poketto.poketto.models.Transactions
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Explorer {
    @GET("poa/dai/api")
    fun transactions(@Query("module") module: String,
                     @Query("action") action: String,
                     @Query("address") address: String) : Call<Transactions>
}