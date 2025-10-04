package com.example.escaneodematerialeskof.domain.gemini

import com.example.escaneodematerialeskof.data.gemini.*

class GeminiRepository {
    private val geminiApi: GeminiApi = GeminiRetrofitClient.instance

    /**
     * Genera contenido de texto usando Gemini
     */
    suspend fun generateText(apiKey: String, prompt: String): GeminiResponse {
        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt)
                    )
                )
            )
        )
        return geminiApi.generateContent(apiKey, request)
    }

    /**
     * Analiza una imagen usando Gemini Vision
     * @param imageBase64 Imagen codificada en Base64
     * @param prompt Pregunta sobre la imagen
     */
    suspend fun analyzeImage(
        apiKey: String,
        imageBase64: String,
        prompt: String,
        mimeType: String = "image/jpeg"
    ): GeminiResponse {
        val request = GeminiImageRequest(
            contents = listOf(
                ImageContent(
                    parts = listOf(
                        ImagePart(text = prompt),
                        ImagePart(
                            inlineData = InlineData(
                                mimeType = mimeType,
                                data = imageBase64
                            )
                        )
                    )
                )
            )
        )
        return geminiApi.analyzeImage(apiKey, request)
    }

    /**
     * Analiza datos de inventario y genera insights
     */
    suspend fun analyzeInventoryData(apiKey: String, inventoryData: String): GeminiResponse {
        val prompt = """
            Analiza los siguientes datos de inventario y proporciona:
            1. Resumen ejecutivo
            2. Productos con mayor rotación
            3. Alertas de stock bajo
            4. Recomendaciones de optimización
            
            Datos de inventario:
            $inventoryData
        """.trimIndent()

        return generateText(apiKey, prompt)
    }

    /**
     * Genera un reporte de discrepancias
     */
    suspend fun generateDiscrepancyReport(
        apiKey: String,
        expectedData: String,
        actualData: String
    ): GeminiResponse {
        val prompt = """
            Compara el inventario esperado vs el inventario real y genera un reporte detallado:
            
            INVENTARIO ESPERADO:
            $expectedData
            
            INVENTARIO REAL:
            $actualData
            
            Por favor indica:
            1. Diferencias encontradas
            2. Posibles causas
            3. Acciones recomendadas
        """.trimIndent()

        return generateText(apiKey, prompt)
    }
}

