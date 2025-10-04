package com.example.escaneodematerialeskof

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.escaneodematerialeskof.data.AppDatabase
import com.example.escaneodematerialeskof.data.ScanHistory
import com.example.escaneodematerialeskof.model.MaterialItem
import com.example.escaneodematerialeskof.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class CapturaInventarioViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context = application.applicationContext
    private val scanHistoryDao = AppDatabase.getDatabase(context).scanHistoryDao()

    private val _modoEscaneo = MutableLiveData<String>("")
    val modoEscaneo: LiveData<String> = _modoEscaneo

    // Base de datos de tarimas por SKU
    var baseTarimas: Map<String, String> = emptyMap()

    /**
     * Establece el modo de escaneo actual.
     */
    fun setModoEscaneo(modo: String) {
        _modoEscaneo.value = modo
    }

    /**
     * Saves a material item to the CSV file and updates the database.
     * Includes proper CSV escaping, data validation, and specific error handling.
     */
    fun guardarMaterialEnArchivo(material: MaterialItem, callback: (Boolean, String) -> Unit) {
        // Validar datos antes de procesar
        if (material.sku.isBlank()) {
            callback(false, "Error: El SKU no puede estar vacío")
            return
        }

        // Validar que totalPallets sea un número positivo
        val pallets = material.totalPallets?.toIntOrNull() ?: 0
        if (pallets < 0) {
            callback(false, "Error: El número de pallets debe ser positivo")
            return
        }

        // Usar un mutex para evitar modificaciones concurrentes al archivo
        viewModelScope.launch(Dispatchers.IO) {
            // Sincronizar acceso al archivo
            csvFileMutex.withLock {
                try {
                    val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                    val sku = material.sku

                    // Leer el archivo existente o crear uno nuevo
                    val allLines = if (file.exists()) {
                        try {
                            file.readLines().toMutableList()
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                callback(false, "Error al leer el archivo CSV: ${e.message}")
                            }
                            return@withLock
                        }
                    } else {
                        mutableListOf()
                    }

                    // Asegurar que el archivo tenga un encabezado sin sobrescribir encabezados extendidos
                    if (allLines.isEmpty()) {
                        allLines.add(Constants.CSV_HEADER)
                    } else if (!allLines[0].startsWith("SKU")) {
                        // Si la primera línea no parece un encabezado, insertar el encabezado por defecto
                        allLines.add(0, Constants.CSV_HEADER)
                    }

                    var found = false
                    var acumulado = pallets

                    // Buscar y actualizar material existente
                    for (i in 1 until allLines.size) {
                        val line = allLines[i]
                        // Usar un parser de CSV adecuado para manejar comas en los valores
                        val cols = parseCsvLine(line).toMutableList()
                        val tipoTarimaCol = cols.getOrNull(12) ?: ""
                        val almacenCol = cols.getOrNull(13) ?: ""

                        if (cols.isNotEmpty() &&
                            cols[0] == sku &&
                            tipoTarimaCol == (material.tipoTarima ?: "") &&
                            almacenCol == (material.almacen ?: "")
                        ) {
                            val prevPallets = cols.getOrNull(11)?.toIntOrNull() ?: 0
                            acumulado = prevPallets + pallets

                            // Actualizar todos los campos, no solo pallets
                            cols[0] = escapeForCsv(material.sku)
                            cols[1] = escapeForCsv(material.descripcion)
                            cols[2] = escapeForCsv(material.cxPal)
                            cols[3] = escapeForCsv(material.fpc)
                            cols[4] = escapeForCsv(material.con)
                            cols[5] = escapeForCsv(material.centro)
                            cols[6] = escapeForCsv(material.linea)
                            cols[7] = escapeForCsv(material.op)
                            cols[8] = escapeForCsv(material.fProd)
                            cols[9] = escapeForCsv(material.diasV)
                            cols[10] = escapeForCsv(material.ubicacion ?: "")
                            cols[11] = acumulado.toString()
                            cols[12] = escapeForCsv(material.tipoTarima ?: "")
                            // Asegurarse de que haya suficientes columnas para el almacén
                            if (cols.size <= 13) {
                                cols.add(escapeForCsv(material.almacen ?: ""))
                            } else {
                                cols[13] = escapeForCsv(material.almacen ?: "")
                            }

                            allLines[i] = cols.joinToString(",")
                            found = true
                            break
                        }
                    }

                    // Agregar nuevo material si no se encontró uno existente
                    if (!found) {
                        val row = listOf(
                            escapeForCsv(material.sku),
                            escapeForCsv(material.descripcion),
                            escapeForCsv(material.cxPal),
                            escapeForCsv(material.fpc),
                            escapeForCsv(material.con),
                            escapeForCsv(material.centro),
                            escapeForCsv(material.linea),
                            escapeForCsv(material.op),
                            escapeForCsv(material.fProd),
                            escapeForCsv(material.diasV),
                            escapeForCsv(material.ubicacion ?: ""),
                            material.totalPallets ?: "",
                            escapeForCsv(material.tipoTarima ?: ""),
                            escapeForCsv(material.almacen ?: "")
                        )
                        allLines.add(row.joinToString(","))
                    }

                    // Escribir el archivo actualizado
                    try {
                        file.writeText(allLines.joinToString("\n"), charset = Charsets.UTF_8)
                    } catch (e: IOException) {
                        withContext(Dispatchers.Main) {
                            callback(false, "Error al escribir el archivo CSV: ${e.message}")
                        }
                        return@withLock
                    }

                    // Guardar en la base de datos
                    try {
                        val scanHistory = ScanHistory(
                            sku = material.sku,
                            description = material.descripcion,
                            quantity = pallets,
                            location = material.ubicacion ?: ""
                        )
                        scanHistoryDao.insertScan(scanHistory)
                    } catch (e: Exception) {
                        // Continuar incluso si hay error en la BD, pero registrar el error
                        Log.e("CapturaViewModel", "Error al guardar en BD: ${e.message}")
                    }

                    // Actualizar la lista de materiales en SharedPreferences para comparación en tiempo real
                    actualizarMaterialesEnSharedPreferences(material, acumulado)

                    withContext(Dispatchers.Main) {
                        callback(true, "Material guardado correctamente. Acumulado: $acumulado pallets")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        callback(false, "Error al guardar: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Escapa un valor para CSV, encerrándolo en comillas si contiene comas o comillas
     */
    private fun escapeForCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    /**
     * Parsea una línea CSV respetando las comillas dobles y comillas escapadas.
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '\"' -> {
                    // Si estamos dentro de comillas y la siguiente también es comilla, es una comilla escapada
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                        current.append('\"')
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
        // Añadir el último campo
        result.add(current.toString())
        return result
    }

    // Mutex para sincronizar acceso al archivo CSV
    private val csvFileMutex = Mutex()

    /**
     * Actualiza la lista de materiales en SharedPreferences para la comparación en tiempo real.
     */
    private fun actualizarMaterialesEnSharedPreferences(material: MaterialItem, acumulado: Int) {
        try {
            val sharedPreferences = context.getSharedPreferences("material_data", Context.MODE_PRIVATE)
            val gson = com.google.gson.Gson()

            // Obtener la lista actual de materiales
            val json = sharedPreferences.getString("materials", "[]")
            val type = object : com.google.gson.reflect.TypeToken<MutableList<MaterialItem>>() {}.type
            val materiales: MutableList<MaterialItem> = gson.fromJson(json, type) ?: mutableListOf()

            // Actualizar o agregar el material escaneado
            val materialConAcumulado =
                material.copy(totalPallets = acumulado.toString(), tipoTarima = material.tipoTarima)

            // Buscar si ya existe un material con el mismo SKU y tipo de tarima
            val index = materiales.indexOfFirst {
                it.sku == material.sku && it.tipoTarima == material.tipoTarima
            }

            if (index >= 0) {
                // Actualizar material existente
                materiales[index] = materialConAcumulado
            } else {
                // Agregar nuevo material
                materiales.add(materialConAcumulado)
            }

            // Guardar la lista actualizada
            val updatedJson = gson.toJson(materiales)
            sharedPreferences.edit().putString("materials", updatedJson).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Sends a material item to the server.
     */
    fun enviarMaterialAlServidor(material: MaterialItem, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // This is a placeholder for actual server communication
                // In a real app, you would implement proper API calls

                // Simulate server communication
                Thread.sleep(500)

                withContext(Dispatchers.Main) {
                    callback(true, "Material enviado al servidor")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, e.message ?: "Error desconocido")
                }
            }
        }
    }

    /**
     * Gets the inventory data from the CSV file.
     */
    fun obtenerDatosInventario(): List<List<String>> {
        val result = mutableListOf<List<String>>()
        try {
            val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
            if (!file.exists()) return result

            val lines = file.readLines()
            for (line in lines) {
                val cols = line.split(",")
                if (cols.size >= 13) // Cambiar de 12 a 13 para incluir TipoTarima
                    result.add(cols)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * Devuelve la lista de materiales escaneados desde el archivo local.
     */
    fun obtenerMaterialesEscaneados(): List<MaterialItem> {
        val lista = mutableListOf<MaterialItem>()
        try {
            val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
            if (!file.exists()) return lista
            val lines = file.readLines()
            if (lines.size <= 1) return lista

            // Obtener inventario de almacén para comparar
            val inventarioAlmacen = obtenerInventarioAlmacen()

            for (i in 1 until lines.size) {
                val cols = parseCsvLine(lines[i])
                if (cols.size >= 12) {
                    val sku = cols[0]
                    val almacen = if (cols.size > 13) cols[13] else ""
                    val totalPalletsEscaneado = cols[11].toIntOrNull() ?: 0

                    // Buscar el inventario disponible para ese SKU y almacén
                    val inventario = inventarioAlmacen.find { it.sku == sku && it.centro == almacen }
                    val disponibleAlmacen = inventario?.disponible ?: 0
                    val restos = (disponibleAlmacen - totalPalletsEscaneado).toString()

                    lista.add(
                        MaterialItem(
                            sku = sku,
                            descripcion = cols[1],
                            cxPal = cols[2],
                            fpc = cols[3],
                            con = cols[4],
                            centro = cols[5],
                            linea = cols[6],
                            op = cols[7],
                            fProd = cols[8],
                            diasV = cols[9],
                            ubicacion = cols[10],
                            totalPallets = cols[11],
                            restos = restos,
                            tipoTarima = if (cols.size > 12) cols[12] else null,
                            almacen = almacen
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("CapturaViewModel", "Error al obtener materiales: ${e.message}")
        }
        return lista
    }

    /**
     * Devuelve la lista de inventario de almacén desde el archivo local.
     */
    fun obtenerInventarioAlmacen(): List<com.example.escaneodematerialeskof.dashboard.InventarioAlmacen> {
        val lista = mutableListOf<com.example.escaneodematerialeskof.dashboard.InventarioAlmacen>()
        try {
            val file = File(context.filesDir, "inventario_almacen.csv")
            if (!file.exists()) {
                // No copiar datos de ejemplo desde assets - solo devolver lista vacía
                return lista
            }

            val lines = file.readLines()
            if (lines.size <= 1) return lista

            for (i in 1 until lines.size) {
                val cols = lines[i].split(",")
                if (cols.size >= 4) {
                    lista.add(
                        com.example.escaneodematerialeskof.dashboard.InventarioAlmacen(
                            sku = cols[0],
                            descripcion = cols[1],
                            disponible = cols[2].toIntOrNull() ?: 0,
                            centro = cols[3]
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lista
    }

    /**
     * Devuelve el mapa base de tarimas por SKU.
     */
    fun obtenerBaseTarimas(): Map<String, String> = baseTarimas

    /**
     * Devuelve un mapa con el acumulado de pallets por SKU y tipo de tarima.
     * Map<SKU, Map<TipoTarima, Cantidad>>
     */
    fun obtenerAcumuladoPorTarima(): Map<String, Map<String, Int>> {
        val resultado = mutableMapOf<String, MutableMap<String, Int>>()
        try {
            val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
            if (!file.exists()) return resultado
            val lines = file.readLines()
            if (lines.size <= 1) return resultado
            for (i in 1 until lines.size) {
                val cols = lines[i].split(",")
                if (cols.size >= 13) {
                    val sku = cols[0]
                    val tipoTarima = cols.getOrNull(12) ?: ""
                    val pallets = cols.getOrNull(11)?.toIntOrNull() ?: 0
                    if (sku.isNotBlank() && tipoTarima.isNotBlank()) {
                        val mapTarima = resultado.getOrPut(sku) { mutableMapOf() }
                        mapTarima[tipoTarima] = (mapTarima[tipoTarima] ?: 0) + pallets
                    }
                }
            }
        } catch (_: Exception) {
        }
        return resultado
    }

    /**
     * Registra la cantidad de pallets por SKU y tipo de tarima (opcional).
     */
    fun registrarTarima(sku: String, cantidad: Int = 1, tipoTarima: String? = null) {
        // Aquí podrías implementar lógica adicional si necesitas guardar el acumulado en memoria
        // Por ahora, solo es un placeholder para compatibilidad
    }

    /**
     * Devuelve la lista actual de materiales escaneados en memoria.
     */
    fun getMaterialesActuales(): List<MaterialItem> {
        // Recupera todos los materiales escaneados actualmente desde la base de datos local
        // o desde la fuente en memoria si existe. Aquí se asume que la tabla ScanHistory almacena los datos.
        // Si tienes una lista en memoria, reemplaza por esa fuente.
        val materiales = mutableListOf<MaterialItem>()
        val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
        if (!file.exists()) return materiales
        val lines = file.readLines()
        if (lines.size <= 1) return materiales // Solo header
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 12) {
                materiales.add(
                    MaterialItem(
                        sku = cols[0],
                        descripcion = cols[1],
                        cxPal = cols[2],
                        fpc = cols[3],
                        con = cols[4],
                        centro = cols[5],
                        linea = cols[6],
                        op = cols[7],
                        fProd = cols[8],
                        diasV = cols[9],
                        ubicacion = cols[10],
                        totalPallets = cols[11],
                        restos = if (cols.size > 12) cols[12] else "",
                        tipoTarima = if (cols.size > 13) cols[13] else null,
                        almacen = if (cols.size > 14) cols[14] else ""
                    )
                )
            }
        }
        return materiales
    }

    /**
     * Exporta el inventario a un archivo CSV en la ubicación indicada por el usuario.
     */
    fun exportarInventario(uri: Uri, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                if (!file.exists()) {
                    withContext(Dispatchers.Main) {
                        callback(false, "No hay inventario para exportar.")
                    }
                    return@launch
                }
                val content = file.readText()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
                withContext(Dispatchers.Main) {
                    callback(true, "Inventario exportado correctamente.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, "Error al exportar: ${e.message}")
                }
            }
        }
    }

    /**
     * Importa un archivo CSV y lo guarda como inventario actual.
     */
    fun importarInventario(uri: Uri, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.readText() ?: ""
                if (content.isBlank()) {
                    withContext(Dispatchers.Main) {
                        callback(false, "El archivo está vacío.")
                    }
                    return@launch
                }

                val lines = content.lines()
                val formattedLines = mutableListOf<String>()

                for (line in lines) {
                    val cols = line.split(",")
                    if (cols.size < 13) {
                        // Ajustar el formato para modo rumba
                        val adjustedLine = line.replace("SKU:", "")
                            .replace("DP:", "")
                            .replace(" ", ",")
                        formattedLines.add(adjustedLine)
                    } else {
                        formattedLines.add(line)
                    }
                }

                val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                file.writeText(formattedLines.joinToString("\n"))

                withContext(Dispatchers.Main) {
                    callback(true, "Inventario importado correctamente.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, "Error al importar: ${e.message}")
                }
            }
        }
    }

    /**
     * Resetea el inventario eliminando el archivo local.
     */
    fun resetearInventario(callback: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                if (file.exists()) file.delete()
                withContext(Dispatchers.Main) {
                    callback(true, "Inventario reseteado correctamente.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, "Error al resetear: ${e.message}")
                }
            }
        }
    }

    /**
     * Devuelve el total de pallets escaneados.
     */
    fun obtenerTotalPallets(): Int {
        return obtenerMaterialesEscaneados().sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
    }

    /**
     * Devuelve un resumen de tarimas por SKU.
     */
    fun resumenTarimas(): Map<String, Int> {
        val resumen = mutableMapOf<String, Int>()
        for (item in obtenerMaterialesEscaneados()) {
            val sku = item.sku
            val pallets = item.totalPallets?.toIntOrNull() ?: 0
            resumen[sku] = (resumen[sku] ?: 0) + pallets
        }
        return resumen
    }

    /**
     * Devuelve un resumen total de pallets por tipo de tarima.
     * Map<TipoTarima, TotalPallets>
     */
    fun obtenerResumenPorTipoTarima(): Map<String, Int> {
        val resumen = mutableMapOf<String, Int>()
        try {
            val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
            if (!file.exists()) return resumen
            val lines = file.readLines()
            if (lines.size <= 1) return resumen
            for (i in 1 until lines.size) {
                val cols = lines[i].split(",")
                if (cols.size >= 13) {
                    val tipoTarima = cols.getOrNull(12)?.trim().orEmpty()
                    val pallets = cols.getOrNull(11)?.toIntOrNull() ?: 0
                    if (tipoTarima.isNotBlank()) {
                        resumen[tipoTarima] = (resumen[tipoTarima] ?: 0) + pallets
                    }
                }
            }
        } catch (_: Exception) {
        }
        return resumen
    }

    /**
     * Realiza la comparación de inventarios usando coroutines apropiadas.
     */
    fun compararInventarios(callback: (Boolean, String, List<com.example.escaneodematerialeskof.dashboard.ComparacionInventario>?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Obtener los datos necesarios para la comparación
                val listaEscaneado: List<MaterialItem> = obtenerMaterialesEscaneados()
                val listaAlmacen: List<com.example.escaneodematerialeskof.dashboard.InventarioAlmacen> =
                    obtenerInventarioAlmacen()
                val baseTarimas: Map<String, String> = obtenerBaseTarimas()

                // Verificar si hay datos para comparar
                if (listaEscaneado.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        callback(false, "No hay datos escaneados para comparar", null)
                    }
                    return@launch
                }

                if (listaAlmacen.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        callback(false, "No hay datos de almacén para comparar", null)
                    }
                    return@launch
                }

                // Generar la comparación
                val comparacion = com.example.escaneodematerialeskof.dashboard.ComparadorInventario.generarComparacion(
                    listaEscaneado, listaAlmacen, baseTarimas
                )

                // Crear un resumen detallado de la comparación
                val resumenBuilder = StringBuilder()

                // Agrupar por estado y contar
                val estadisticas = comparacion.groupingBy { it.estado }.eachCount()

                // Añadir estadísticas generales
                resumenBuilder.append("RESUMEN GENERAL:\n\n")
                estadisticas.entries.forEach {
                    when {
                        it.key.contains("OK") -> resumenBuilder.append("✅ ${it.key}: ${it.value}\n")
                        it.key.contains("Faltante") -> resumenBuilder.append("❌ ${it.key}: ${it.value}\n")
                        it.key.contains("Sobrante") -> resumenBuilder.append("🔄 ${it.key}: ${it.value}\n")
                        else -> resumenBuilder.append("• ${it.key}: ${it.value}\n")
                    }
                }

                // Añadir totales
                val totalEscaneado = comparacion.sumOf { it.escaneado ?: 0 }
                val totalInventario = comparacion.sumOf { it.inventario ?: 0 }
                val diferencia = totalEscaneado - totalInventario

                resumenBuilder.append("\nTOTALES:\n")
                resumenBuilder.append("📊 Total escaneado: $totalEscaneado\n")
                resumenBuilder.append("📊 Total en inventario: $totalInventario\n")
                resumenBuilder.append("📊 Diferencia total: $diferencia\n")

                withContext(Dispatchers.Main) {
                    callback(true, resumenBuilder.toString(), comparacion)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, "Error al comparar: ${e.message}", null)
                }
            }
        }
    }

    /**
     * Obtiene el total de pallets esperados según el inventario del sistema.
     */
    fun obtenerTotalPalletsEsperados(): Int {
        // Esta función debería comparar con un inventario de referencia
        // Por ahora retorna un valor estimado basado en los datos actuales
        val sharedPreferences = context.getSharedPreferences("inventario_sistema", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("total_pallets_esperados", 1000) // Valor por defecto
    }

    /**
     * Obtiene el número de discrepancias críticas (diferencias > 10%).
     */
    fun obtenerDiscrepanciasCriticas(): Int {
        // Lógica para detectar discrepancias críticas
        val materialesEscaneados = obtenerMaterialesEscaneados()
        var discrepancias = 0

        // Aquí podrías comparar con inventario del sistema si está disponible
        // Por ahora, simula algunas discrepancias basadas en ciertos criterios
        materialesEscaneados.forEach { material ->
            val pallets = material.totalPallets?.toIntOrNull() ?: 0
            // Ejemplo: considerar discrepancia si hay más de 50 pallets de un mismo SKU
            if (pallets > 50) {
                discrepancias++
            }
        }

        return discrepancias
    }

    /**
     * Genera alertas inteligentes basadas en el inventario actual.
     */
    fun generarAlertas(): List<String> {
        val alertas = mutableListOf<String>()
        val materiales = obtenerMaterialesEscaneados()

        // Alerta por SKUs duplicados con ubicaciones diferentes
        val skusPorUbicacion = materiales.groupBy { it.sku }
        skusPorUbicacion.forEach { (sku, items) ->
            val ubicaciones = items.mapNotNull { it.ubicacion }.distinct()
            if (ubicaciones.size > 1) {
                alertas.add("⚠️ SKU $sku encontrado en ${ubicaciones.size} ubicaciones diferentes")
            }
        }

        // Alerta por cantidades anómalas
        materiales.forEach { material ->
            val pallets = material.totalPallets?.toIntOrNull() ?: 0
            if (pallets > 100) {
                alertas.add("🚨 Cantidad alta: ${material.sku} tiene $pallets pallets")
            }
        }

        // Alerta por tipos de tarima no estándar
        val tiposNoEstandar = materiales.filter {
            val tipo = it.tipoTarima?.uppercase()
            tipo != null && tipo !in listOf("PLASTICO", "MADERA", "METAL")
        }
        if (tiposNoEstandar.isNotEmpty()) {
            alertas.add("📦 ${tiposNoEstandar.size} items con tipo de tarima no estándar")
        }

        return alertas
    }

    /**
     * Actualiza la capacidad del almacén actual con los pallets escaneados
     */
    fun actualizarCapacidadAlmacen() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val almacenManager = com.example.escaneodematerialeskof.manager.AlmacenCapacidadManager(context)
                val totalPallets = obtenerTotalPallets()
                almacenManager.actualizarPalletsDesdeInventario(totalPallets)
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }

    /**
     * Elimina la última fila (material guardado) del archivo CSV de inventario.
     */
    fun eliminarUltimoMaterialGuardado(callback: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            csvFileMutex.withLock {
                try {
                    val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                    if (!file.exists()) {
                        withContext(Dispatchers.Main) {
                            callback(false, "No hay archivo de inventario para modificar")
                        }
                        return@withLock
                    }
                    val lines = file.readLines().toMutableList()
                    if (lines.size <= 1) {
                        withContext(Dispatchers.Main) {
                            callback(false, "No hay registros para eliminar")
                        }
                        return@withLock
                    }
                    // Eliminar la última línea no vacía (mantener encabezado)
                    var idx = lines.lastIndex
                    while (idx > 0 && lines[idx].isBlank()) idx--
                    if (idx <= 0) {
                        withContext(Dispatchers.Main) {
                            callback(false, "No hay registros para eliminar")
                        }
                        return@withLock
                    }
                    lines.removeAt(idx)
                    try {
                        file.writeText(lines.joinToString("\n"))
                    } catch (e: IOException) {
                        withContext(Dispatchers.Main) {
                            callback(false, "Error al escribir cambios: ${e.message}")
                        }
                        return@withLock
                    }
                    withContext(Dispatchers.Main) {
                        callback(true, "Último registro eliminado")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        callback(false, "Error al eliminar: ${e.message}")
                    }
                }
            }
        }
    }
}
