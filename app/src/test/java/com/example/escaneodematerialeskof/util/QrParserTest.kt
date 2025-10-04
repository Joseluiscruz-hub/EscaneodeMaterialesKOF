package com.example.escaneodematerialeskof.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QrParserTest {

    @Test
    fun `parse clave valor completo`() {
        val qr = """
            SKU:12345
            DP:Producto Premium
            CxPal:56
            FPC:2025-01-01
            Con:LoteX
            Centro:CT01
            LINEA:L1
            OP:OP7788
            FProd:2024-12-30
            Dias V:90
        """.trimIndent()
        val datos = QrParser.parse(qr)
        assertEquals("12345", datos["SKU"])
        assertEquals("Producto Premium", datos["DP"])
        assertEquals("56", datos["CxPal"])
        assertEquals("2025-01-01", datos["FPC"])
        assertEquals(10, datos.size)
    }

    @Test
    fun `parse con posiciones fallback 10 lineas`() {
        val qr = (1..10).joinToString("\n") { "VAL$it" }
        val m = QrParser.parse(qr)
        assertEquals("VAL1", m["SKU"])
        assertEquals("VAL2", m["DP"])
        assertEquals("VAL3", m["CxPal"])
        assertEquals("VAL10", m["Dias V"])
    }

    @Test
    fun `parse con posiciones fallback 3 lineas`() {
        val qr = "A\nB\nC"
        val m = QrParser.parse(qr)
        assertEquals("A", m["SKU"])
        assertEquals("B", m["DP"])
        assertEquals("C", m["CxPal"])
        assertNull(m["FPC"]) // No debe existir
    }

    @Test
    fun `parse con posiciones fallback 2 lineas`() {
        val qr = "SKUONLY\nDESC"
        val m = QrParser.parse(qr)
        assertEquals("SKUONLY", m["SKU"])
        assertEquals("DESC", m["DP"])
        assertNull(m["CxPal"])
    }

    @Test
    fun `parse con sola linea`() {
        val qr = "UNICO"
        val m = QrParser.parse(qr)
        assertEquals("UNICO", m["SKU"])
        assertNull(m["DP"])
    }

    @Test
    fun `parse ignora lineas vacias`() {
        val qr = "\n\nSKU: 999\n\nDP:Test\n\n"
        val m = QrParser.parse(qr)
        assertEquals("999", m["SKU"])
        assertEquals("Test", m["DP"])
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parse falla sin sku clave valor`() {
        val qr = "DP:SoloDescripcion" // No hay SKU
        QrParser.parse(qr)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parse falla vacio`() {
        QrParser.parse("")
    }
}

