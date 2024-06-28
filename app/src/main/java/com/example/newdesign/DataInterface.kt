package com.example.newdesign

import retrofit2.Call
import retrofit2.http.GET

interface DataInterface {
    @GET("comments")
    fun getEmail(): Call<List<DataClassItem>>
}