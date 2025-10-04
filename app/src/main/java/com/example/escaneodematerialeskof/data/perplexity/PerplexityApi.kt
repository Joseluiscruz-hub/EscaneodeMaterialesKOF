package com.example.escaneodematerialeskof.data.perplexity

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PerplexityApi {
    @POST("chat/completions")
    suspend fun getAnswer(
        @Header("Authorization") token: String,
        @Body request: PerplexityRequest
    ): PerplexityResponse
}

