package com.example.escaneodematerialeskof.data.gemini

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    @SerializedName("contents")
    val contents: List<Content>
)

data class Content(
    @SerializedName("parts")
    val parts: List<Part>
)

data class Part(
    @SerializedName("text")
    val text: String
)

// Para análisis de imágenes
data class GeminiImageRequest(
    @SerializedName("contents")
    val contents: List<ImageContent>
)

data class ImageContent(
    @SerializedName("parts")
    val parts: List<ImagePart>
)

data class ImagePart(
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("inline_data")
    val inlineData: InlineData? = null
)

data class InlineData(
    @SerializedName("mime_type")
    val mimeType: String,
    @SerializedName("data")
    val data: String // Base64 encoded image
)

