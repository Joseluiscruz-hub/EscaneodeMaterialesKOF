package com.example.escaneodematerialeskof

import androidx.annotation.RawRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.airbnb.lottie.compose.*
import com.example.escaneodematerialeskof.ui.theme.FemsaBackground

@Composable
fun HomeScreen(
    onTipoEscaneo: () -> Unit,
    onCompararInventarios: () -> Unit,
    onAjustesInventario: () -> Unit,
    onMasOpciones: () -> Unit,
    onCapturaInventario: () -> Unit,
    onComparar: () -> Unit,
    onResumen: () -> Unit,
    onImportar: () -> Unit,
    onExportar: () -> Unit,
    onConfiguracion: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isTablet = screenWidthDp > 600.dp
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val isCompact = screenWidthDp < 400.dp
    val isPreview = LocalInspectionMode.current

    // Padding y espaciado responsivo
    val padding = when {
        isCompact -> 12.dp
        isTablet -> 24.dp
        else -> 16.dp
    }

    val cardPadding = when {
        isCompact -> 12.dp
        isTablet -> 20.dp
        else -> 16.dp
    }

    val spacerHeight = when {
        isCompact -> 12.dp
        isTablet -> 24.dp
        else -> 16.dp
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        // Fondo con gradiente + partículas animadas
        FemsaBackground {
            Box(modifier = Modifier.fillMaxSize()) {
                // Partículas de fondo
                if (!isPreview) {
                    val particlesComposition by rememberLottieComposition(LottieCompositionSpec.Asset("background_particles.json"))
                    val particlesProgress by animateLottieCompositionAsState(
                        composition = particlesComposition,
                        iterations = LottieConstants.IterateForever
                    )

                    LottieAnimation(
                        composition = particlesComposition,
                        progress = { particlesProgress },
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.6f)
                            .zIndex(0f),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                // Contenido principal encima de las partículas
                if (isTablet && isLandscape) {
                    // Layout de tablet en horizontal - Grid de 2 columnas
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .zIndex(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Header(
                                logoSize = if (isTablet) 120.dp else 80.dp,
                                spacerHeight = spacerHeight,
                                useLargeTitle = true
                            )
                        }

                        item {
                            // Grid de 2 columnas para tablet horizontal
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Columna izquierda
                                Column(modifier = Modifier.weight(1f)) {
                                    MainActionsCard(
                                        onTipoEscaneo = onTipoEscaneo,
                                        onCapturaInventario = onCapturaInventario,
                                        onCompararInventarios = onCompararInventarios,
                                        cardPadding = cardPadding,
                                        isTablet = isTablet
                                    )

                                    Spacer(modifier = Modifier.height(spacerHeight))

                                    ManagementCard(
                                        onAjustesInventario = onAjustesInventario,
                                        onResumen = onResumen,
                                        cardPadding = cardPadding,
                                        isTablet = isTablet
                                    )
                                }

                                // Columna derecha
                                Column(modifier = Modifier.weight(1f)) {
                                    UtilitiesCard(
                                        onImportar = onImportar,
                                        onExportar = onExportar,
                                        onConfiguracion = onConfiguracion,
                                        onMasOpciones = onMasOpciones,
                                        onComparar = onComparar,
                                        cardPadding = cardPadding,
                                        isTablet = isTablet
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Layout normal - una columna con scroll
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .zIndex(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Header(
                            logoSize = if (isTablet) 100.dp else 70.dp,
                            spacerHeight = spacerHeight,
                            useLargeTitle = isTablet
                        )

                        // Main actions section
                        MainActionsCard(
                            onTipoEscaneo = onTipoEscaneo,
                            onCapturaInventario = onCapturaInventario,
                            onCompararInventarios = onCompararInventarios,
                            cardPadding = cardPadding,
                            isTablet = isTablet
                        )

                        Spacer(modifier = Modifier.height(spacerHeight))

                        // Secondary actions section
                        ManagementCard(
                            onAjustesInventario = onAjustesInventario,
                            onResumen = onResumen,
                            cardPadding = cardPadding,
                            isTablet = isTablet
                        )

                        Spacer(modifier = Modifier.height(spacerHeight))

                        // Utility actions section
                        UtilitiesCard(
                            onImportar = onImportar,
                            onExportar = onExportar,
                            onConfiguracion = onConfiguracion,
                            onMasOpciones = onMasOpciones,
                            onComparar = onComparar,
                            cardPadding = cardPadding,
                            isTablet = isTablet
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainActionsCard(
    onTipoEscaneo: () -> Unit,
    onCapturaInventario: () -> Unit,
    onCompararInventarios: () -> Unit,
    cardPadding: androidx.compose.ui.unit.Dp,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Acciones principales",
                style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = if (isTablet) 12.dp else 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                LottieIconButton(
                    lottieResId = R.raw.scan_animation,
                    text = "Escanear",
                    onClick = onTipoEscaneo,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
                LottieIconButton(
                    lottieResId = R.raw.inventory_animation,
                    text = "Capturar",
                    onClick = onCapturaInventario,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
                LottieIconButton(
                    lottieResId = R.raw.compare_animation,
                    text = "Comparar",
                    onClick = onCompararInventarios,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ManagementCard(
    onAjustesInventario: () -> Unit,
    onResumen: () -> Unit,
    cardPadding: androidx.compose.ui.unit.Dp,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Gestión de inventario",
                style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = if (isTablet) 12.dp else 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                LottieIconButton(
                    lottieResId = R.raw.settings_animation,
                    text = "Ajustes",
                    onClick = onAjustesInventario,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
                LottieIconButton(
                    lottieResId = R.raw.summary_animation,
                    text = "Resumen",
                    onClick = onResumen,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun UtilitiesCard(
    onImportar: () -> Unit,
    onExportar: () -> Unit,
    onConfiguracion: () -> Unit,
    onMasOpciones: () -> Unit,
    onComparar: () -> Unit,
    cardPadding: androidx.compose.ui.unit.Dp,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Utilidades",
                style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = if (isTablet) 12.dp else 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                LottieIconButton(
                    lottieResId = R.raw.import_animation,
                    text = "Importar",
                    onClick = onImportar,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
                LottieIconButton(
                    lottieResId = R.raw.export_animation,
                    text = "Exportar",
                    onClick = onExportar,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
                LottieIconButton(
                    lottieResId = R.raw.more_animation,
                    text = "Más",
                    onClick = onMasOpciones,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                LottieIconButton(
                    lottieResId = R.raw.settings_animation,
                    text = "Configuración",
                    onClick = onConfiguracion,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
                LottieIconButton(
                    lottieResId = R.raw.compare_animation,
                    text = "Comparar ahora",
                    onClick = onComparar,
                    isTablet = isTablet,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Header(
    logoSize: androidx.compose.ui.unit.Dp,
    spacerHeight: androidx.compose.ui.unit.Dp,
    useLargeTitle: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))

        val isPreview = LocalInspectionMode.current
        if (!isPreview) {
            val logoComposition by rememberLottieComposition(LottieCompositionSpec.Asset("logo_animation.json"))
            val logoProgress by animateLottieCompositionAsState(
                composition = logoComposition,
                iterations = LottieConstants.IterateForever
            )

            LottieAnimation(
                composition = logoComposition,
                progress = { logoProgress },
                modifier = Modifier.size(logoSize)
            )
        } else {
            Box(modifier = Modifier.size(logoSize))
        }

        Text(
            "KOF Inventario",
            style = if (useLargeTitle) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(spacerHeight))
    }
}

@Composable
private fun LottieIconButton(
    @RawRes lottieResId: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false
) {
    val isPreview = LocalInspectionMode.current
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isPreview) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(if (isTablet) 70.dp else 50.dp)
            )
        } else {
            Box(modifier = Modifier.size(if (isTablet) 70.dp else 50.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = if (isTablet) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    com.example.escaneodematerialeskof.ui.theme.FemsaTheme {
        HomeScreen(
            onTipoEscaneo = {},
            onCompararInventarios = {},
            onAjustesInventario = {},
            onMasOpciones = {},
            onCapturaInventario = {},
            onComparar = {},
            onResumen = {},
            onImportar = {},
            onExportar = {},
            onConfiguracion = {}
        )
    }
}
