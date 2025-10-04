package com.example.escaneodematerialeskof.util

/**
 * Parser reutilizable para códigos QR de inventario.
 * Intenta primero extraer pares clave:valor; si no, asigna por posición.
 */
object QrParser {
    private val PREFIJOS = listOf("SKU", "DP", "CxPal", "FPC", "Con", "Centro", "LINEA", "OP", "FProd", "Dias V")

    /**
     * Parsea el texto de un QR y devuelve un mapa de campos limpiados.
     * @throws IllegalArgumentException si no se puede extraer un SKU.
     */
    fun parse(qrText: String): Map<String, String> {
        if (qrText.isBlank()) throw IllegalArgumentException("El código QR está vacío")
        val datos = mutableMapOf<String, String>()

        // Paso 1: formato clave:valor o patrones conocidos
        qrText.lines().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach
            val idx = line.indexOf(":")
            if (idx != -1) {
                val key = line.substring(0, idx).trim()
                val value = line.substring(idx + 1).trim()
                if (key.isNotEmpty()) datos[key] = value
            } else {
                val centroRegex = Regex("^Centro:(\\S+)", RegexOption.IGNORE_CASE)
                val lineaRegex = Regex("^LINEA:(\\S+)", RegexOption.IGNORE_CASE)
                centroRegex.find(line)?.let { datos["Centro"] = it.groupValues[1] }
                lineaRegex.find(line)?.let { datos["LINEA"] = it.groupValues[1] }
            }
        }

        // Paso 2: fallback por posiciones si nada reconocido
        if (datos.isEmpty()) {
            val lines = qrText.lines().map { it.trim() }.filter { it.isNotEmpty() }
            when {
                lines.size >= 10 -> {
                    datos["SKU"] = lines[0]; datos["DP"] = lines[1]; datos["CxPal"] = lines[2]; datos["FPC"] = lines[3]
                    datos["Con"] = lines[4]; datos["Centro"] = lines[5]; datos["LINEA"] = lines[6]; datos["OP"] =
                        lines[7]
                    datos["FProd"] = lines[8]; datos["Dias V"] = lines[9]
                }

                lines.size >= 3 -> {
                    datos["SKU"] = lines[0]; datos["DP"] = lines[1]; datos["CxPal"] = lines[2]
                }

                lines.size >= 2 -> {
                    datos["SKU"] = lines[0]; datos["DP"] = lines[1]
                }

                lines.isNotEmpty() -> datos["SKU"] = lines[0]
            }
        }

        if (datos["SKU"].isNullOrBlank()) throw IllegalArgumentException("No se pudo extraer el SKU del código QR")
        return limpiarPrefijos(datos)
    }

    private fun limpiarPrefijos(datos: Map<String, String>): Map<String, String> = datos.mapValues { (k, v) ->
        val prefijo = PREFIJOS.find { k.equals(it, ignoreCase = true) }
        if (prefijo != null && v.startsWith("$prefijo:")) v.removePrefix("$prefijo:").trim() else v.trim()
    }
}

