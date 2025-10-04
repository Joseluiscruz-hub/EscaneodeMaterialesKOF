package com.example.escaneodematerialeskof.data.gemini

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val instance: GeminiApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(GeminiApi::class.java)
    }
}

