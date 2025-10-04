package com.example.escaneodematerialeskof.domain.perplexity

import com.example.escaneodematerialeskof.data.perplexity.PerplexityApi
import com.example.escaneodematerialeskof.data.perplexity.PerplexityRequest
import com.example.escaneodematerialeskof.data.perplexity.PerplexityResponse
import com.example.escaneodematerialeskof.data.perplexity.RetrofitClient

class PerplexityRepository {
    private val perplexityApi: PerplexityApi = RetrofitClient.instance

    suspend fun getAnswer(token: String, input: String): PerplexityResponse {
        val request = PerplexityRequest(input)
        return perplexityApi.getAnswer(token, request)
    }
}

