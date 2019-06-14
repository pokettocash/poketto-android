package com.poketto.poketto

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Explorer {
    @GET("poa/dai/api")
    fun transactions(@Query("module") module: String,
                     @Query("action") action: String,
                     @Query("address") address: String) : Call<Transactions>
}