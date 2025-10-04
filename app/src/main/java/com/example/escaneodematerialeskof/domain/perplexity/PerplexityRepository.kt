package com.example.escaneodematerialeskof.domain.perplexity

import com.example.escaneodematerialeskof.data.perplexity.*

class PerplexityRepository {
    private val perplexityApi: PerplexityApi = RetrofitClient.instance

    suspend fun getAnswer(token: String, input: String): PerplexityResponse {
        val messages = listOf(
            Message(role = "system", content = "Eres un asistente experto en gestión de inventarios y logística."),
            Message(role = "user", content = input)
        )
        val request = PerplexityRequest(messages = messages)
        return perplexityApi.getAnswer(token, request)
    }
}

