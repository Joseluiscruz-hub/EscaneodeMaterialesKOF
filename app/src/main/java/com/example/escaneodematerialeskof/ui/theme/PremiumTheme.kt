package com.example.escaneodematerialeskof.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Colores Coca Cola Femsa
val femsaRed = Color(0xFFED1C24)
val femsaWhite = Color(0xFFFFFFFF)
val femsaBlack = Color(0xFF222222)
val femsaGray = Color(0xFFB0B0B0)
val femsaSilver = Color(0xFFE5E4E2)
val femsaGold = Color(0xFFFFD700)
val femsaAccent = Color(0xFFB388FF)

private val LightFemsaColorScheme = lightColorScheme(
    primary = femsaRed,
    onPrimary = femsaWhite,
    primaryContainer = femsaSilver,
    onPrimaryContainer = femsaBlack,
    secondary = femsaGold,
    onSecondary = femsaBlack,
    background = femsaWhite,
    onBackground = femsaBlack,
    surface = femsaSilver,
    onSurface = femsaBlack,
    outline = femsaGray,
    surfaceVariant = femsaWhite,
    onSurfaceVariant = femsaBlack
)

private val DarkFemsaColorScheme = darkColorScheme(
    primary = femsaRed,
    onPrimary = femsaWhite,
    primaryContainer = femsaBlack,
    onPrimaryContainer = femsaRed,
    secondary = femsaGold,
    onSecondary = femsaBlack,
    background = femsaBlack,
    onBackground = femsaRed,
    surface = femsaBlack,
    onSurface = femsaRed,
    outline = femsaGray,
    surfaceVariant = femsaGray,
    onSurfaceVariant = femsaRed
)

@Composable
fun FemsaTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkFemsaColorScheme else LightFemsaColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

@Composable
fun FemsaBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.background(
            brush = Brush.horizontalGradient(
                listOf(femsaRed, femsaGold)
            )
        )
    ) {
        content()
    }
}
