package com.example.escaneodematerialeskof.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import kotlin.math.roundToInt

/**
 * Burbuja flotante con una mini calculadora básica.
 * - Se puede arrastrar en pantalla.
 * - Al pulsar se expande una calculadora con operaciones simples.
 */
@Composable
fun FloatingCalculatorBubble(
    modifier: Modifier = Modifier,
    bubbleColor: Color = Color(0xFFD32F2F),
    panelColor: Color = Color.White,
    contentColor: Color = Color(0xFF222222)
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidthPx = with(density) { screenWidth.toPx() }
    val screenHeightPx = with(density) { screenHeight.toPx() }

    // Estado de posición de la burbuja
    var bubbleOffset by remember { mutableStateOf(Offset(40f, 200f)) }
    var expanded by remember { mutableStateOf(false) }

    // Estado de la calculadora
    var display by remember { mutableStateOf("0") }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var justCalculated by remember { mutableStateOf(false) }

    fun inputDigit(d: String) {
        if (justCalculated) {
            display = d
            justCalculated = false
            return
        }
        display = if (display == "0") d else display + d
    }

    fun inputDot() {
        if (justCalculated) {
            display = "0."
            justCalculated = false
            return
        }
        if (!display.contains('.')) display += "."
    }

    fun clearAll() {
        display = "0"
        firstOperand = null
        pendingOp = null
        justCalculated = false
    }

    fun backspace() {
        if (justCalculated) return
        display = if (display.length <= 1) "0" else display.dropLast(1)
    }

    fun applyOp(op: String) {
        try {
            val current = display.toDoubleOrNull() ?: return
            if (firstOperand == null) {
                firstOperand = current
            } else if (pendingOp != null) {
                val result = compute(firstOperand!!, current, pendingOp!!)
                firstOperand = result
                display = trimResult(result)
            }
            pendingOp = op
            justCalculated = true
        } catch (_: Exception) {
        }
    }

    fun equals() {
        try {
            val current = display.toDoubleOrNull() ?: return
            if (firstOperand != null && pendingOp != null) {
                val result = compute(firstOperand!!, current, pendingOp!!)
                display = trimResult(result)
                firstOperand = null
                pendingOp = null
                justCalculated = true
            }
        } catch (_: Exception) {
        }
    }

    @Composable
    fun CalcButton(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colors.primary,
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(backgroundColor = color, contentColor = Color.White),
            modifier = modifier.height(44.dp),
            shape = RoundedCornerShape(8.dp)
        ) { Text(text, fontSize = 16.sp) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Burbuja flotante
        Box(
            modifier = Modifier
                .offset { IntOffset(bubbleOffset.x.roundToInt(), bubbleOffset.y.roundToInt()) }
                .size(56.dp)
                .background(bubbleColor, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        bubbleOffset = Offset(
                            (bubbleOffset.x + dragAmount.x).coerceIn(
                                0f,
                                screenWidthPx - with(density) { 56.dp.toPx() }),
                            (bubbleOffset.y + dragAmount.y).coerceIn(
                                0f,
                                screenHeightPx - with(density) { 56.dp.toPx() })
                        )
                    }
                }
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Calculate, contentDescription = "Calculator", tint = Color.White)
        }

        if (expanded) {
            Popup(alignment = Alignment.TopStart, onDismissRequest = { expanded = false }) {
                val panelWidth = 280.dp
                val panelWidthPx = with(density) { panelWidth.toPx() }
                val panelHeightPx = with(density) { 360.dp.toPx() }
                val panelOffsetX = bubbleOffset.x.coerceIn(0f, screenWidthPx - panelWidthPx)
                val panelOffsetY = (bubbleOffset.y + 70f).coerceIn(0f, screenHeightPx - panelHeightPx)
                Card(
                    modifier = Modifier
                        .offset { IntOffset(panelOffsetX.roundToInt(), panelOffsetY.roundToInt()) }
                        .width(panelWidth),
                    backgroundColor = panelColor,
                    shape = RoundedCornerShape(16.dp),
                    elevation = 12.dp
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Calculadora",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = contentColor
                            )
                            IconButton(onClick = { expanded = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar")
                            }
                        }
                        Text(
                            text = display,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            color = contentColor,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                        // Filas de botones
                        val opColor = Color(0xFF607D8B)
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CalcButton(
                                    "C",
                                    modifier = Modifier.weight(1f),
                                    color = Color(0xFFD32F2F)
                                ) { clearAll() }
                                CalcButton("←", modifier = Modifier.weight(1f), color = opColor) { backspace() }
                                CalcButton("/", modifier = Modifier.weight(1f), color = opColor) { applyOp("/") }
                                CalcButton("*", modifier = Modifier.weight(1f), color = opColor) { applyOp("*") }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CalcButton("7", modifier = Modifier.weight(1f)) { inputDigit("7") }
                                CalcButton("8", modifier = Modifier.weight(1f)) { inputDigit("8") }
                                CalcButton("9", modifier = Modifier.weight(1f)) { inputDigit("9") }
                                CalcButton("-", modifier = Modifier.weight(1f), color = opColor) { applyOp("-") }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CalcButton("4", modifier = Modifier.weight(1f)) { inputDigit("4") }
                                CalcButton("5", modifier = Modifier.weight(1f)) { inputDigit("5") }
                                CalcButton("6", modifier = Modifier.weight(1f)) { inputDigit("6") }
                                CalcButton("+", modifier = Modifier.weight(1f), color = opColor) { applyOp("+") }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CalcButton("1", modifier = Modifier.weight(1f)) { inputDigit("1") }
                                CalcButton("2", modifier = Modifier.weight(1f)) { inputDigit("2") }
                                CalcButton("3", modifier = Modifier.weight(1f)) { inputDigit("3") }
                                CalcButton("=", modifier = Modifier.weight(1f), color = Color(0xFF388E3C)) { equals() }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CalcButton("0", modifier = Modifier.weight(2f)) { inputDigit("0") }
                                CalcButton(".", modifier = Modifier.weight(1f)) { inputDot() }
                                CalcButton("=", modifier = Modifier.weight(1f), color = Color(0xFF388E3C)) { equals() }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun compute(a: Double, b: Double, op: String): Double = when (op) {
    "+" -> a + b
    "-" -> a - b
    "*" -> a * b
    "/" -> if (b == 0.0) Double.NaN else a / b
    else -> b
}

private fun trimResult(value: Double): String {
    if (value.isNaN()) return "Err"
    val long = value.toLong()
    return if (value == long.toDouble()) long.toString() else value.toString()
}
