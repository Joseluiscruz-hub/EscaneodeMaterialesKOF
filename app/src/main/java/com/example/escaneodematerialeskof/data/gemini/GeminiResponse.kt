package com.example.escaneodematerialeskof.data.gemini

import com.google.gson.annotations.SerializedName

data class GeminiResponse(
    @SerializedName("candidates")
    val candidates: List<Candidate>
)

data class Candidate(
    @SerializedName("content")
    val content: ResponseContent,
    @SerializedName("finishReason")
    val finishReason: String?,
    @SerializedName("safetyRatings")
    val safetyRatings: List<SafetyRating>?
)

data class ResponseContent(
    @SerializedName("parts")
    val parts: List<ResponsePart>,
    @SerializedName("role")
    val role: String
)

data class ResponsePart(
    @SerializedName("text")
    val text: String
)

data class SafetyRating(
    @SerializedName("category")
    val category: String,
    @SerializedName("probability")
    val probability: String
)

