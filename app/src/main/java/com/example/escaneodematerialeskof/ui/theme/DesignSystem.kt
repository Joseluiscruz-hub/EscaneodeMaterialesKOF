package com.example.escaneodematerialeskof.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Sistema de Diseño Unificado para Escaneo de Materiales KOF
 * Basado en Material Design 3 y Guía de Marca Coca-Cola FEMSA
 */
object DesignSystem {

    /**
     * Paleta de Colores Corporativos FEMSA
     */
    object Colors {
        // Colores Primarios - Rojo Coca-Cola
        val Primary = Color(0xFFD32F2F)
        val PrimaryVariant = Color(0xFFB71C1C)
        val PrimaryLight = Color(0xFFEF5350)
        val PrimaryDark = Color(0xFF9A0007)

        // Colores Secundarios - Grises Corporativos
        val Secondary = Color(0xFF37474F)
        val SecondaryVariant = Color(0xFF263238)
        val SecondaryLight = Color(0xFF546E7A)

        // Colores de Estado
        val Success = Color(0xFF4CAF50)
        val SuccessLight = Color(0xFF81C784)
        val SuccessDark = Color(0xFF388E3C)

        val Warning = Color(0xFFFF9800)
        val WarningLight = Color(0xFFFFB74D)
        val WarningDark = Color(0xFFF57C00)

        val Error = Color(0xFFF44336)
        val ErrorLight = Color(0xFFE57373)
        val ErrorDark = Color(0xFFD32F2F)

        val Info = Color(0xFF2196F3)
        val InfoLight = Color(0xFF64B5F6)
        val InfoDark = Color(0xFF1976D2)

        // Colores de Fondo
        val Background = Color(0xFFF5F5F5)
        val BackgroundDark = Color(0xFF121212)
        val Surface = Color.White
        val SurfaceDark = Color(0xFF1E1E1E)

        // Colores de Texto
        val TextPrimary = Color(0xFF212121)
        val TextSecondary = Color(0xFF757575)
        val TextDisabled = Color(0xFFBDBDBD)
        val TextOnPrimary = Color.White
        val TextOnDark = Color.White

        // Colores de Tipo de Tarima
        val TarimaKOF = Color(0xFF1976D2)      // Azul
        val TarimaSAMS = Color(0xFF388E3C)     // Verde
        val TarimaIEQSA = Color(0xFFF57C00)    // Naranja
        val TarimaCHEP = Color(0xFF7B1FA2)     // Morado

        // Colores de Gráficos (Accesibles)
        val ChartColors = listOf(
            Primary,
            Info,
            Success,
            Warning,
            Color(0xFF9C27B0),  // Morado
            Color(0xFF00BCD4),  // Cian
            Color(0xFF795548),  // Marrón
            Color(0xFF607D8B)   // Azul Gris
        )

        // Colores de Saturación (Almacén)
        val SaturationLow = Success       // < 50%
        val SaturationMedium = Warning    // 50-75%
        val SaturationHigh = Color(0xFFFF9800)  // 75-90%
        val SaturationCritical = Error    // > 90%
    }

    /**
     * Sistema de Espaciado Consistente
     * Todos los espaciados son múltiplos de 4dp
     */
    object Spacing {
        val none: Dp = 0.dp
        val xxs: Dp = 2.dp
        val xs: Dp = 4.dp
        val sm: Dp = 8.dp
        val md: Dp = 16.dp
        val lg: Dp = 24.dp
        val xl: Dp = 32.dp
        val xxl: Dp = 40.dp
        val xxxl: Dp = 48.dp

        // Espaciados específicos de componentes
        val cardPadding: Dp = md
        val screenPadding: Dp = md
        val itemSpacing: Dp = sm
        val sectionSpacing: Dp = lg
    }

    /**
     * Sistema Tipográfico
     */
    object Typography {
        // Display - Para títulos muy grandes
        val displayLarge = TextStyle(
            fontSize = 57.sp,
            lineHeight = 64.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.25).sp
        )

        val displayMedium = TextStyle(
            fontSize = 45.sp,
            lineHeight = 52.sp,
            fontWeight = FontWeight.Bold
        )

        val displaySmall = TextStyle(
            fontSize = 36.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.Bold
        )

        // Headline - Para encabezados de secciones
        val headlineLarge = TextStyle(
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Bold
        )

        val headlineMedium = TextStyle(
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Bold
        )

        val headlineSmall = TextStyle(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold
        )

        // Title - Para títulos de cards y diálogos
        val titleLarge = TextStyle(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.SemiBold
        )

        val titleMedium = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.15.sp
        )

        val titleSmall = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp
        )

        // Body - Para contenido principal
        val bodyLarge = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.5.sp
        )

        val bodyMedium = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.25.sp
        )

        val bodySmall = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.4.sp
        )

        // Label - Para botones y etiquetas
        val labelLarge = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp
        )

        val labelMedium = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )

        val labelSmall = TextStyle(
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    }

    /**
     * Elevaciones y Sombras
     */
    object Elevation {
        val none: Dp = 0.dp
        val level1: Dp = 1.dp
        val level2: Dp = 2.dp
        val level3: Dp = 4.dp
        val level4: Dp = 6.dp
        val level5: Dp = 8.dp
        val level6: Dp = 12.dp
        val level7: Dp = 16.dp
    }

    /**
     * Radios de Esquinas
     */
    object CornerRadius {
        val none: Dp = 0.dp
        val xs: Dp = 4.dp
        val sm: Dp = 8.dp
        val md: Dp = 12.dp
        val lg: Dp = 16.dp
        val xl: Dp = 20.dp
        val xxl: Dp = 24.dp
        val full: Dp = 9999.dp

        // Radios específicos de componentes
        val button: Dp = md
        val card: Dp = lg
        val dialog: Dp = xl
        val bottomSheet: Dp = xxl
    }

    /**
     * Tamaños de Iconos
     */
    object IconSize {
        val xs: Dp = 16.dp
        val sm: Dp = 20.dp
        val md: Dp = 24.dp
        val lg: Dp = 32.dp
        val xl: Dp = 40.dp
        val xxl: Dp = 48.dp
        val xxxl: Dp = 64.dp

        // Tamaños específicos
        val appBar: Dp = md
        val button: Dp = lg
        val fab: Dp = md
        val avatar: Dp = xl
    }

    /**
     * Duraciones de Animaciones (en milisegundos)
     */
    object Duration {
        const val instant = 0
        const val fast = 150
        const val medium = 300
        const val slow = 500
        const val verySlow = 800

        // Duraciones específicas
        const val fadeIn = fast
        const val fadeOut = fast
        const val slideIn = medium
        const val slideOut = medium
        const val scaleIn = fast
        const val scaleOut = fast
    }

    /**
     * Tamaños de Componentes
     */
    object ComponentSize {
        // Botones
        val buttonHeightSmall: Dp = 40.dp
        val buttonHeightMedium: Dp = 48.dp
        val buttonHeightLarge: Dp = 56.dp

        // Campos de texto
        val textFieldHeight: Dp = 56.dp
        val textFieldHeightSmall: Dp = 48.dp

        // App Bar
        val appBarHeight: Dp = 56.dp
        val appBarHeightLarge: Dp = 64.dp

        // Bottom Navigation
        val bottomNavHeight: Dp = 80.dp

        // FAB
        val fabSize: Dp = 56.dp
        val fabSizeSmall: Dp = 40.dp
        val fabSizeLarge: Dp = 96.dp

        // Cards
        val cardMinHeight: Dp = 80.dp
        val cardImageHeight: Dp = 180.dp

        // List Items
        val listItemHeight: Dp = 72.dp
        val listItemHeightSmall: Dp = 56.dp
    }

    /**
     * Opacidades
     */
    object Alpha {
        const val disabled = 0.38f
        const val inactive = 0.60f
        const val active = 1.0f
        const val overlay = 0.12f
        const val hover = 0.08f
        const val pressed = 0.16f
    }

    /**
     * Z-Index para superposición de elementos
     */
    object ZIndex {
        const val background = 0f
        const val content = 1f
        const val appBar = 10f
        const val fab = 20f
        const val snackbar = 30f
        const val drawer = 40f
        const val modal = 50f
        const val dialog = 60f
        const val tooltip = 70f
        const val loading = 100f
    }
}
