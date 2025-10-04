package com.example.escaneodematerialeskof.manager

import android.content.Context
import android.content.SharedPreferences
import com.example.escaneodematerialeskof.model.AlmacenCapacidad
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manager para gestionar la persistencia de los almacenes configurados
 */
class AlmacenCapacidadManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("almacen_capacidad", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_ALMACENES = "almacenes_configurados"
        private const val KEY_ALMACEN_ACTUAL = "almacen_actual_seleccionado"
    }

    /**
     * Guarda la lista de almacenes configurados
     */
    fun guardarAlmacenes(almacenes: List<AlmacenCapacidad>) {
        val json = gson.toJson(almacenes)
        prefs.edit().putString(KEY_ALMACENES, json).apply()
    }

    /**
     * Obtiene la lista de almacenes configurados
     */
    fun obtenerAlmacenes(): List<AlmacenCapacidad> {
        val json = prefs.getString(KEY_ALMACENES, null) ?: return emptyList()
        val type = object : TypeToken<List<AlmacenCapacidad>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Agrega un nuevo almacén o actualiza uno existente
     */
    fun agregarOActualizarAlmacen(almacen: AlmacenCapacidad) {
        val almacenes = obtenerAlmacenes().toMutableList()
        val index = almacenes.indexOfFirst { it.nombreAlmacen == almacen.nombreAlmacen }

        if (index >= 0) {
            almacenes[index] = almacen
        } else {
            almacenes.add(almacen)
        }

        guardarAlmacenes(almacenes)
    }

    /**
     * Elimina un almacén por nombre
     */
    fun eliminarAlmacen(nombreAlmacen: String) {
        val almacenes = obtenerAlmacenes().toMutableList()
        almacenes.removeAll { it.nombreAlmacen == nombreAlmacen }
        guardarAlmacenes(almacenes)
    }

    /**
     * Actualiza los pallets escaneados de un almacén específico
     */
    fun actualizarPalletsEscaneados(nombreAlmacen: String, palletsEscaneados: Int) {
        val almacenes = obtenerAlmacenes().toMutableList()
        val index = almacenes.indexOfFirst { it.nombreAlmacen == nombreAlmacen }

        if (index >= 0) {
            val almacenActualizado = almacenes[index].copy(palletsEscaneados = palletsEscaneados)
            almacenes[index] = almacenActualizado
            guardarAlmacenes(almacenes)
        }
    }

    /**
     * Actualiza los pallets escaneados de todos los almacenes basado en el inventario total
     */
    fun actualizarPalletsDesdeInventario(totalPalletsEscaneados: Int) {
        val almacenes = obtenerAlmacenes().toMutableList()
        val almacenActual = obtenerAlmacenActual()

        if (almacenActual != null && almacenes.isNotEmpty()) {
            val index = almacenes.indexOfFirst { it.nombreAlmacen == almacenActual }
            if (index >= 0) {
                val almacenActualizado = almacenes[index].copy(palletsEscaneados = totalPalletsEscaneados)
                almacenes[index] = almacenActualizado
                guardarAlmacenes(almacenes)
            }
        }
    }

    /**
     * Establece el almacén actual donde se está escaneando
     */
    fun establecerAlmacenActual(nombreAlmacen: String) {
        prefs.edit().putString(KEY_ALMACEN_ACTUAL, nombreAlmacen).apply()
    }

    /**
     * Obtiene el nombre del almacén actual
     */
    fun obtenerAlmacenActual(): String? {
        return prefs.getString(KEY_ALMACEN_ACTUAL, null)
    }

    /**
     * Obtiene un almacén específico por nombre
     */
    fun obtenerAlmacenPorNombre(nombreAlmacen: String): AlmacenCapacidad? {
        return obtenerAlmacenes().find { it.nombreAlmacen == nombreAlmacen }
    }

    /**
     * Calcula el resumen total de todos los almacenes
     */
    fun obtenerResumenTotal(): ResumenAlmacenes {
        val almacenes = obtenerAlmacenes()
        val totalPalletsEscaneados = almacenes.sumOf { it.palletsEscaneados }
        val totalCapacidad = almacenes.sumOf { it.capacidadMaxima }
        val saturacionPromedio = if (totalCapacidad > 0) {
            (totalPalletsEscaneados.toDouble() / totalCapacidad) * 100
        } else 0.0

        return ResumenAlmacenes(
            totalAlmacenes = almacenes.size,
            totalPalletsEscaneados = totalPalletsEscaneados,
            totalCapacidadConfigurada = totalCapacidad,
            saturacionPromedio = saturacionPromedio
        )
    }
}

/**
 * Data class para el resumen total de almacenes
 */
data class ResumenAlmacenes(
    val totalAlmacenes: Int,
    val totalPalletsEscaneados: Int,
    val totalCapacidadConfigurada: Int,
    val saturacionPromedio: Double
) {
    val saturacionPromedioFormateada: String
        get() = String.format("%.1f%%", saturacionPromedio)
}
