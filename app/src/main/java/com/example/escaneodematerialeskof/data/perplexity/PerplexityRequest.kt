package com.example.escaneodematerialeskof.data.perplexity

import com.google.gson.annotations.SerializedName

data class PerplexityRequest(
    @SerializedName("model")
    val model: String = "llama-3.1-sonar-small-128k-online",
    @SerializedName("messages")
    val messages: List<Message>
)

data class Message(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)

