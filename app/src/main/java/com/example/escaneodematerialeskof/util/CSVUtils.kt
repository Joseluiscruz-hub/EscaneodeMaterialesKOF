package com.example.escaneodematerialeskof.util

/** Utilidades para manejo de CSV reutilizadas en distintos ViewModels */
object CSVUtils {
    /**
     * Parsea una línea CSV respetando comillas y comillas escapadas.
     */
    fun parseCsvLine(line: String): List<String> {
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
