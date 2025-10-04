package com.example.escaneodematerialeskof.ui.inventario

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.escaneodematerialeskof.util.CSVUtils
import com.example.escaneodematerialeskof.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel para la pantalla de Inventario (Compose) que calcula métricas dinámicas
 * a partir del archivo CSV de materiales.
 */
class InventarioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(InventarioUIState())
    val uiState: StateFlow<InventarioUIState> = _uiState.asStateFlow()

    private var filtroAlmacen: String? = null

    fun setAlmacen(almacen: String?) {
        filtroAlmacen = almacen?.takeIf { it.isNotBlank() && it != "Seleccionar Almacén" }
    }

    fun cargar(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            withContext(Dispatchers.IO) {
                try {
                    val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                    if (!file.exists()) {
                        _uiState.value = InventarioUIState(cargando = false, ultimaActualizacion = ahora())
                        return@withContext
                    }
                    val lineas = file.readLines(Charsets.UTF_8)
                    if (lineas.size <= 1) {
                        _uiState.value = InventarioUIState(cargando = false, ultimaActualizacion = ahora())
                        return@withContext
                    }
                    val registros = lineas.drop(1)
                        .map { CSVUtils.parseCsvLine(it) }
                        .filter { it.size >= 13 }
                        .filter { registro ->
                            filtroAlmacen?.let { fa -> registro.getOrNull(13)?.trim() == fa } ?: true
                        }

                    val totalPallets = registros.sumOf { it.getOrNull(11)?.toIntOrNull() ?: 0 }
                    val totalCajas = registros.sumOf { reg ->
                        val pallets = reg.getOrNull(11)?.toIntOrNull() ?: 0
                        val cxPal = reg.getOrNull(2)?.toIntOrNull() ?: 0
                        pallets * cxPal
                    }
                    val totalSkus = registros.map { it[0] }.distinct().size
                    val ubicacionesDistintas =
                        registros.mapNotNull { it.getOrNull(10)?.takeIf { u -> u.isNotBlank() } }.distinct().size
                    val promedioPalletsPorSku = if (totalSkus > 0) totalPallets.toDouble() / totalSkus else 0.0
                    val progreso =
                        if (Constants.CAPACIDAD_PALLETS_OBJETIVO > 0) totalPallets.toFloat() / Constants.CAPACIDAD_PALLETS_OBJETIVO else 0f

                    val ultimo = registros.lastOrNull()
                    val ultimoSku = ultimo?.getOrNull(0) ?: ""
                    val ultimoPallets = ultimo?.getOrNull(11)?.toIntOrNull() ?: 0
                    val ultimoUbicacion = ultimo?.getOrNull(10) ?: ""

                    val ultimosEscaneos = registros.takeLast(5).map {
                        EscaneoBreve(
                            sku = it[0],
                            pallets = it.getOrNull(11)?.toIntOrNull() ?: 0,
                            ubicacion = it.getOrNull(10) ?: ""
                        )
                    }.reversed()

                    _uiState.value = InventarioUIState(
                        cargando = false,
                        totalPallets = totalPallets,
                        totalCajas = totalCajas,
                        totalSkus = totalSkus,
                        ultimoSku = ultimoSku,
                        ultimoPallets = ultimoPallets,
                        ultimoUbicacion = ultimoUbicacion,
                        almacenFiltrado = filtroAlmacen,
                        ultimaActualizacion = ahora(),
                        progreso = progreso.coerceIn(0f, 1f),
                        ultimosEscaneos = ultimosEscaneos,
                        promedioPalletsPorSku = promedioPalletsPorSku,
                        ubicacionesDistintas = ubicacionesDistintas
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = e.message ?: "Error desconocido"
                    )
                }
            }
        }
    }

    private fun ahora(): String = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(Date())

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i += 2
                        continue
                    }
                    inQuotes = !inQuotes
                }

                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.setLength(0)
                }

                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}

/**
 * Estado UI para la pantalla de Inventario.
 */
data class InventarioUIState(
    val cargando: Boolean = false,
    val totalPallets: Int = 0,
    val totalCajas: Int = 0,
    val totalSkus: Int = 0,
    val ultimoSku: String = "",
    val ultimoPallets: Int = 0,
    val ultimoUbicacion: String = "",
    val almacenFiltrado: String? = null,
    val ultimaActualizacion: String = "",
    val progreso: Float = 0f,
    val ultimosEscaneos: List<EscaneoBreve> = emptyList(),
    val promedioPalletsPorSku: Double = 0.0,
    val ubicacionesDistintas: Int = 0,
    val error: String? = null
)

data class EscaneoBreve(
    val sku: String,
    val pallets: Int,
    val ubicacion: String
)
