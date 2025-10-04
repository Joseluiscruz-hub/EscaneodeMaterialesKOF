package com.example.escaneodematerialeskof.ui.dashboard

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.escaneodematerialeskof.util.CSVUtils
import com.example.escaneodematerialeskof.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel para el Dashboard Ejecutivo
 * Procesa datos del inventario y genera métricas inteligentes
 */
class DashboardEjecutivoViewModel : ViewModel() {

    // Estado
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    // Snapshot anterior para calcular tendencias
    private var previousTotalPallets: Int? = null
    private var previousTotalSkus: Int? = null
    private var previousAlertKeys: Set<String> = emptySet()

    // Monitoreo en tiempo real
    private var monitoringJob: Job? = null

    // Preferencias
    private var prefs: SharedPreferences? = null

    // Filtros actuales
    private var filtroAlmacen: String? = null
    private var filtroTarima: String? = null

    // Intervalo monitoreo
    private var monitoringIntervalMs: Long = 10000L

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(DASHBOARD_PREFS, Context.MODE_PRIVATE)
            previousTotalPallets = prefs?.getInt(KEY_LAST_TOTAL_PALLETS, 0)
            previousTotalSkus = prefs?.getInt(KEY_LAST_TOTAL_SKUS, 0)
        }
    }

    fun updateMonitoringInterval(context: Context, newIntervalMs: Long) {
        monitoringIntervalMs = newIntervalMs.coerceAtLeast(3000L)
        stopRealtimeMonitoring()
        startRealtimeMonitoring(context, monitoringIntervalMs)
    }

    fun setFilters(almacen: String?, tarima: String?) {
        filtroAlmacen = almacen?.takeIf { it.isNotBlank() && it != "Todos" }
        filtroTarima = tarima?.takeIf { it.isNotBlank() && it != "Todos" }
    }

    override fun onCleared() {
        super.onCleared()
        stopRealtimeMonitoring()
    }

    fun startRealtimeMonitoring(context: Context, intervalMs: Long = monitoringIntervalMs) {
        if (monitoringJob?.isActive == true) return
        monitoringJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    actualizarSoloAlertas(context)
                } catch (_: Exception) {
                }
                delay(intervalMs)
            }
        }
    }

    fun stopRealtimeMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }

    /** Carga todos los datos del dashboard desde el archivo CSV */
    fun cargarDatosDashboard(context: Context) {
        init(context)
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true)
            withContext(Dispatchers.IO) {
                try {
                    val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                    if (!file.exists()) {
                        _dashboardState.value = DashboardState(
                            isLoading = false,
                            ultimaActualizacion = obtenerFechaActual()
                        )
                        return@withContext
                    }
                    val lines = file.readLines()
                    if (lines.size <= 1) {
                        _dashboardState.value = DashboardState(
                            isLoading = false,
                            ultimaActualizacion = obtenerFechaActual()
                        )
                        return@withContext
                    }

                    val materialesData = lines.drop(1).mapNotNull { line ->
                        CSVUtils.parseCsvLine(line).takeIf { it.size >= 14 }
                    }

                    // Conjuntos completos para filtros
                    val almacenesDisponibles =
                        materialesData.mapNotNull { it.getOrNull(13)?.takeIf { a -> a.isNotBlank() } }.distinct()
                            .sorted()
                    val tarimasDisponibles =
                        materialesData.mapNotNull { it.getOrNull(12)?.takeIf { t -> t.isNotBlank() } }.distinct()
                            .sorted()

                    // Filtrado
                    val filtrados = materialesData.filter { cols ->
                        val okAlmacen = filtroAlmacen?.let { cols.getOrNull(13) == it } ?: true
                        val okTarima = filtroTarima?.let { cols.getOrNull(12) == it } ?: true
                        okAlmacen && okTarima
                    }

                    val totalSKUs = filtrados.map { it[0] }.distinct().size
                    val totalPallets = filtrados.sumOf { it.getOrNull(11)?.toIntOrNull() ?: 0 }
                    val almacenes = filtrados.mapNotNull { it.getOrNull(13) }.filter { it.isNotBlank() }.distinct()

                    val resumenAlmacenes = calcularResumenAlmacenes(filtrados)
                    val resumenTiposTarima = calcularResumenTiposTarima(filtrados)
                    val alertas = generarAlertasInteligentes(filtrados)
                    val topSKUs = calcularTopSKUs(filtrados)

                    val capacidadTotal = 5000
                    val tasaOcupacion =
                        if (capacidadTotal > 0) (totalPallets.toDouble() / capacidadTotal) * 100 else 0.0

                    val tendenciaPallets =
                        previousTotalPallets?.let { prev -> if (prev == 0) null else ((totalPallets - prev).toDouble() / prev) * 100.0 }
                    val tendenciaSkus =
                        previousTotalSkus?.let { prev -> if (prev == 0) null else ((totalSKUs - prev).toDouble() / prev) * 100.0 }

                    val totalPalletsSafe = totalPallets.takeIf { it > 0 } ?: 1
                    val distribucionTarimasPct =
                        resumenTiposTarima.mapValues { (it.value.toDouble() / totalPalletsSafe) * 100.0 }
                    val distribucionAlmacenesPct =
                        resumenAlmacenes.associate { it.name to (it.items.toDouble() / totalPalletsSafe) * 100.0 }

                    // Historial pallets
                    val historyRaw = prefs?.getString(KEY_HISTORY_PALLETS, "")?.takeIf { it!!.isNotBlank() }
                    val historyList =
                        historyRaw?.split(",")?.mapNotNull { it.toIntOrNull() }?.toMutableList() ?: mutableListOf()
                    if (historyList.isEmpty() || historyList.last() != totalPallets) {
                        historyList.add(totalPallets)
                    }
                    while (historyList.size > HISTORY_LIMIT) historyList.removeAt(0)
                    prefs?.edit()?.putString(KEY_HISTORY_PALLETS, historyList.joinToString(","))?.apply()

                    // Alertas tiempo real (clave mensaje+ubicacion)
                    val currentAlertKeys = alertas.map { it.mensaje + "@" + it.ubicacion }.toSet()
                    val nuevasAlertasKeys = currentAlertKeys - previousAlertKeys
                    val nuevasAlertas = alertas.filter { (it.mensaje + "@" + it.ubicacion) in nuevasAlertasKeys }

                    _dashboardState.value = DashboardState(
                        isLoading = false,
                        totalSKUs = totalSKUs,
                        totalPallets = totalPallets,
                        almacenesActivos = almacenes.size,
                        alertasCriticas = alertas.size,
                        resumenAlmacenes = resumenAlmacenes,
                        resumenTiposTarima = resumenTiposTarima,
                        alertas = alertas,
                        topSKUs = topSKUs,
                        tasaOcupacion = tasaOcupacion,
                        ultimaActualizacion = obtenerFechaActual(),
                        tendenciaTotalPallets = tendenciaPallets,
                        tendenciaTotalSkus = tendenciaSkus,
                        distribucionTarimasPct = distribucionTarimasPct,
                        distribucionAlmacenesPct = distribucionAlmacenesPct,
                        realtimeAlerts = nuevasAlertas,
                        nuevasAlertasCount = nuevasAlertas.size,
                        historialPallets = historyList.toList(),
                        filtroAlmacen = filtroAlmacen,
                        filtroTarima = filtroTarima,
                        almacenesDisponibles = almacenesDisponibles,
                        tarimasDisponibles = tarimasDisponibles,
                        monitoringIntervalMs = monitoringIntervalMs
                    )

                    previousTotalPallets = totalPallets
                    previousTotalSkus = totalSKUs
                    previousAlertKeys = currentAlertKeys

                    prefs?.edit()?.apply {
                        putInt(KEY_LAST_TOTAL_PALLETS, totalPallets)
                        putInt(KEY_LAST_TOTAL_SKUS, totalSKUs)
                        apply()
                    }
                } catch (e: Exception) {
                    _dashboardState.value = _dashboardState.value.copy(
                        isLoading = false,
                        error = "Error al cargar datos: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun actualizarSoloAlertas(context: Context) {
        val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
        if (!file.exists()) return
        val lines = file.readLines()
        if (lines.size <= 1) return
        val datos = lines.drop(1).mapNotNull { parseCsvLine(it).takeIf { cols -> cols.size >= 12 } }
        val alertas = generarAlertasInteligentes(datos)
        val currentAlertKeys = alertas.map { it.mensaje + "@" + it.ubicacion }.toSet()
        val nuevas = currentAlertKeys - previousAlertKeys
        if (nuevas.isNotEmpty()) {
            val nuevasAlertas = alertas.filter { (it.mensaje + "@" + it.ubicacion) in nuevas }
            _dashboardState.value = _dashboardState.value.copy(
                realtimeAlerts = nuevasAlertas,
                nuevasAlertasCount = nuevasAlertas.size,
                alertas = alertas,
                alertasCriticas = alertas.size,
                ultimaActualizacion = obtenerFechaActual()
            )
            previousAlertKeys = currentAlertKeys
        }
    }

    private fun calcularResumenAlmacenes(datos: List<List<String>>): List<AlmacenData> {
        val almacenesMap = mutableMapOf<String, Int>()

        datos.forEach { cols ->
            val almacen = cols.getOrNull(13)?.trim() ?: "Sin Almacén"
            val pallets = cols.getOrNull(11)?.toIntOrNull() ?: 0
            almacenesMap[almacen] = (almacenesMap[almacen] ?: 0) + pallets
        }

        val totalPallets = almacenesMap.values.sum()

        return almacenesMap.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { entry ->
                val percentage = if (totalPallets > 0) {
                    ((entry.value.toDouble() / totalPallets) * 100).toInt()
                } else 0

                val color = when {
                    percentage >= 80 -> Color(0xFF4CAF50)
                    percentage >= 60 -> Color(0xFFFF9800)
                    else -> Color(0xFFD32F2F)
                }

                AlmacenData(
                    name = entry.key.ifBlank { "Sin Almacén" },
                    items = entry.value,
                    percentage = percentage,
                    statusColor = color
                )
            }
    }

    private fun calcularResumenTiposTarima(datos: List<List<String>>): Map<String, Int> {
        val tiposMap = mutableMapOf<String, Int>()

        datos.forEach { cols ->
            val tipo = cols.getOrNull(12)?.trim() ?: "Sin Clasificar"
            val pallets = cols.getOrNull(11)?.toIntOrNull() ?: 0
            if (tipo.isNotBlank()) {
                tiposMap[tipo] = (tiposMap[tipo] ?: 0) + pallets
            }
        }

        return tiposMap.entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }
    }

    private fun generarAlertasInteligentes(datos: List<List<String>>): List<AlertaInteligente> {
        val alertas = mutableListOf<AlertaInteligente>()
        val ahora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        // Agrupar por SKU
        val skusPorUbicacion = datos.groupBy { it[0] }

        // Detectar SKUs en múltiples ubicaciones
        skusPorUbicacion.forEach { (sku, items) ->
            val ubicaciones = items.mapNotNull { it.getOrNull(10) }.distinct()
            if (ubicaciones.size > 1) {
                alertas.add(
                    AlertaInteligente(
                        mensaje = "SKU $sku encontrado en ${ubicaciones.size} ubicaciones",
                        ubicacion = ubicaciones.joinToString(", "),
                        fechaHora = ahora,
                        tipo = TipoAlerta.UBICACION_MULTIPLE
                    )
                )
            }
        }

        // Detectar cantidades altas
        datos.forEach { cols ->
            val sku = cols[0]
            val pallets = cols.getOrNull(11)?.toIntOrNull() ?: 0
            val ubicacion = cols.getOrNull(10) ?: "Sin ubicación"

            if (pallets > 100) {
                alertas.add(
                    AlertaInteligente(
                        mensaje = "Cantidad alta: $sku con $pallets pallets",
                        ubicacion = ubicacion,
                        fechaHora = ahora,
                        tipo = TipoAlerta.CANTIDAD_ALTA
                    )
                )
            }
        }

        // Detectar tipos de tarima no estándar
        datos.forEach { cols ->
            val sku = cols[0]
            val tipoTarima = cols.getOrNull(12)?.trim()?.uppercase()
            val ubicacion = cols.getOrNull(10) ?: "Sin ubicación"

            if (tipoTarima != null && tipoTarima !in listOf("PLASTICO", "MADERA", "METAL", "")) {
                alertas.add(
                    AlertaInteligente(
                        mensaje = "Tipo de tarima no estándar: $tipoTarima para SKU $sku",
                        ubicacion = ubicacion,
                        fechaHora = ahora,
                        tipo = TipoAlerta.TARIMA_NO_ESTANDAR
                    )
                )
            }
        }

        return alertas.take(10) // Limitar a 10 alertas más importantes
    }

    private fun calcularTopSKUs(datos: List<List<String>>): List<TopSKUData> {
        val skusMap = mutableMapOf<String, Pair<String, Int>>() // SKU -> (Descripción, Total Pallets)

        datos.forEach { cols ->
            val sku = cols[0]
            val descripcion = cols.getOrNull(1) ?: ""
            val pallets = cols.getOrNull(11)?.toIntOrNull() ?: 0

            val current = skusMap[sku]
            if (current == null) {
                skusMap[sku] = descripcion to pallets
            } else {
                skusMap[sku] = current.first to (current.second + pallets)
            }
        }

        return skusMap.entries
            .sortedByDescending { it.value.second }
            .take(5)
            .map { entry ->
                TopSKUData(
                    sku = entry.key,
                    descripcion = entry.value.first.take(30), // Limitar descripción
                    pallets = entry.value.second
                )
            }
    }

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

    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}

/**
 * Estado del Dashboard
 */
data class DashboardState(
    val isLoading: Boolean = false,
    val totalSKUs: Int = 0,
    val totalPallets: Int = 0,
    val almacenesActivos: Int = 0,
    val alertasCriticas: Int = 0,
    val resumenAlmacenes: List<AlmacenData> = emptyList(),
    val resumenTiposTarima: Map<String, Int> = emptyMap(),
    val alertas: List<AlertaInteligente> = emptyList(),
    val topSKUs: List<TopSKUData> = emptyList(),
    val tasaOcupacion: Double = 0.0,
    val ultimaActualizacion: String = "",
    val error: String? = null,
    val tendenciaTotalPallets: Double? = null,
    val tendenciaTotalSkus: Double? = null,
    val distribucionTarimasPct: Map<String, Double> = emptyMap(),
    val distribucionAlmacenesPct: Map<String, Double> = emptyMap(),
    val realtimeAlerts: List<AlertaInteligente> = emptyList(),
    val nuevasAlertasCount: Int = 0,
    // Nuevos campos
    val historialPallets: List<Int> = emptyList(),
    val filtroAlmacen: String? = null,
    val filtroTarima: String? = null,
    val almacenesDisponibles: List<String> = emptyList(),
    val tarimasDisponibles: List<String> = emptyList(),
    val monitoringIntervalMs: Long = 10000L
)

/**
 * Data class para alertas inteligentes
 */
data class AlertaInteligente(
    val mensaje: String,
    val ubicacion: String,
    val fechaHora: String,
    val tipo: TipoAlerta
)

enum class TipoAlerta {
    UBICACION_MULTIPLE,
    CANTIDAD_ALTA,
    TARIMA_NO_ESTANDAR,
    STOCK_BAJO
}

/**
 * Data class para Top SKUs
 */
data class TopSKUData(
    val sku: String,
    val descripcion: String,
    val pallets: Int
)

/**
 * Data class para datos de almacén
 */
data class AlmacenData(
    val name: String,
    val items: Int,
    val percentage: Int,
    val statusColor: Color
)

// Preferencias
private const val DASHBOARD_PREFS = "dashboard_prefs"
private const val KEY_LAST_TOTAL_PALLETS = "last_total_pallets"
private const val KEY_LAST_TOTAL_SKUS = "last_total_skus"
private const val KEY_HISTORY_PALLETS = "history_pallets" // CSV de enteros
private const val HISTORY_LIMIT = 40

