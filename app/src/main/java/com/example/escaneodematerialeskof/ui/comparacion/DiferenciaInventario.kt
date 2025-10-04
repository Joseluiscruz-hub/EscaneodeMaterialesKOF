package com.example.escaneodematerialeskof.ui.comparacion

/**
 * Clase de datos para representar una diferencia encontrada en el inventario
 */
data class DiferenciaInventario(
    val codigo: String,
    val descripcion: String,
    val cantidadReferencia: Int,
    val cantidadActual: Int,
    val diferencia: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val estado: EstadoDiferencia = when {
        diferencia > 0 -> EstadoDiferencia.EXCESO
        diferencia < 0 -> EstadoDiferencia.FALTANTE
        else -> EstadoDiferencia.COINCIDE
    }
)

/**
 * Enum para clasificar el tipo de diferencia
 */
enum class EstadoDiferencia {
    COINCIDE,
    FALTANTE,
    EXCESO
}
