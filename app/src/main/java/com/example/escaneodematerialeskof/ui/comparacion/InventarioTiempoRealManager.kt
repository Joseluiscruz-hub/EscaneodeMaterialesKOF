package com.example.escaneodematerialeskof.ui.comparacion

import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * Gestor para simular datos de inventario en tiempo real
 * En una implementación real, esto podría conectarse a:
 * - Un servicio de escáner de códigos de barras
 * - Una base de datos en tiempo real
 * - Un servidor que proporciona actualizaciones
 */
class InventarioTiempoRealManager {

    private var isActive = false
    private var job: Job? = null
    private var onDatoRecibido: ((ItemInventario) -> Unit)? = null

    /**
     * Clase para representar un item de inventario escaneado
     */
    data class ItemInventario(
        val codigo: String,
        val descripcion: String,
        val cantidad: Int,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Inicia la simulación de datos en tiempo real
     */
    fun iniciarMonitoreo(
        inventarioReferencia: List<Map<String, String>>,
        onDatoRecibido: (ItemInventario) -> Unit
    ) {
        if (isActive) return

        isActive = true
        this.onDatoRecibido = onDatoRecibido

        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                // Simular llegada de datos cada 3-7 segundos
                delay(Random.nextLong(3000, 7000))

                if (isActive && inventarioReferencia.isNotEmpty()) {
                    // Seleccionar un item aleatorio del inventario de referencia
                    val itemReferencia = inventarioReferencia.random()

                    // Generar cantidad actual (puede tener diferencias)
                    val cantidadReferencia = itemReferencia["cantidad"]?.toIntOrNull() ?: 0
                    val cantidadActual = simularCantidadActual(cantidadReferencia)

                    val itemEscaneado = ItemInventario(
                        codigo = itemReferencia["codigo"] ?: "",
                        descripcion = itemReferencia["descripcion"] ?: "",
                        cantidad = cantidadActual
                    )

                    onDatoRecibido?.invoke(itemEscaneado)
                }
            }
        }
    }

    /**
     * Detiene el monitoreo en tiempo real
     */
    fun detenerMonitoreo() {
        isActive = false
        job?.cancel()
        job = null
    }

    /**
     * Simula variaciones en la cantidad actual respecto a la referencia
     */
    private fun simularCantidadActual(cantidadReferencia: Int): Int {
        return when (Random.nextInt(0, 100)) {
            in 0..70 -> cantidadReferencia // 70% coincide exactamente
            in 71..85 -> maxOf(0, cantidadReferencia - Random.nextInt(1, 6)) // 15% faltante
            in 86..95 -> cantidadReferencia + Random.nextInt(1, 4) // 10% exceso
            else -> Random.nextInt(0, cantidadReferencia + 10) // 5% diferencia aleatoria
        }
    }

    /**
     * Verifica si el monitoreo está activo
     */
    fun isMonitoreando(): Boolean = isActive
}
