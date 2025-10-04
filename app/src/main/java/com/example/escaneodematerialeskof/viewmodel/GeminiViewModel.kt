package com.example.escaneodematerialeskof.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.escaneodematerialeskof.data.gemini.GeminiResponse
import com.example.escaneodematerialeskof.domain.gemini.GeminiRepository
import kotlinx.coroutines.launch

class GeminiViewModel : ViewModel() {

    private val repository = GeminiRepository()

    private val _response = MutableLiveData<GeminiResponse>()
    val response: LiveData<GeminiResponse> = _response

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    /**
     * Genera texto usando Gemini
     */
    fun generateText(apiKey: String, prompt: String) {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                val result = repository.generateText(apiKey, prompt)
                _response.postValue(result)
            } catch (e: Exception) {
                _error.postValue("Error al consultar Gemini: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    /**
     * Analiza una imagen usando Gemini Vision
     */
    fun analyzeImage(apiKey: String, imageBase64: String, prompt: String) {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                val result = repository.analyzeImage(apiKey, imageBase64, prompt)
                _response.postValue(result)
            } catch (e: Exception) {
                _error.postValue("Error al analizar imagen: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    /**
     * Analiza datos de inventario
     */
    fun analyzeInventory(apiKey: String, inventoryData: String) {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                val result = repository.analyzeInventoryData(apiKey, inventoryData)
                _response.postValue(result)
            } catch (e: Exception) {
                _error.postValue("Error al analizar inventario: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    /**
     * Genera reporte de discrepancias
     */
    fun generateDiscrepancyReport(apiKey: String, expected: String, actual: String) {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                val result = repository.generateDiscrepancyReport(apiKey, expected, actual)
                _response.postValue(result)
            } catch (e: Exception) {
                _error.postValue("Error al generar reporte: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }
}
        @Body request: GeminiRequest
    ): GeminiResponse

    // Para análisis de imágenes
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun analyzeImage(
        @Query("key") apiKey: String,
        @Body request: GeminiImageRequest
    ): GeminiResponse
}

