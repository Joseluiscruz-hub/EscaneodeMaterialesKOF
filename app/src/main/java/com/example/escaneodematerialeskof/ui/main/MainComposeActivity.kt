package com.example.escaneodematerialeskof.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.escaneodematerialeskof.ui.theme.DesignSystem
import com.example.escaneodematerialeskof.ui.components.FEMSABadge

// Data class para representar cada botón de las funciones principales
private data class FunctionItem(val icon: ImageVector, val label: String, val onClick: () -> Unit)

class MainComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                App()
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun App() {
    val context = LocalContext.current

    // Colores personalizados de la interfaz
    val backgroundColor = Color(0xFFF0F2F5) // Un gris muy claro para el fondo
    val cardBackgroundColor = Color.White
    val primaryTextColor = Color(0xFF333333)
    val secondaryTextColor = Color(0xFF666666)
    val redButtonColor = Color(0xFFD32F2F)
    val darkButtonColor = Color(0xFF37474F)

    // Lista de funciones principales con sus iconos y etiquetas
    val mainFunctions = listOf(
        FunctionItem(Icons.Default.PhoneAndroid, "Capturar Inventario") {
            context.startActivity(
                Intent(
                    context,
                    com.example.escaneodematerialeskof.ui.inventario.InventarioComposeActivity::class.java
                )
            )
        },
        FunctionItem(Icons.Default.BarChart, "Dashboard") {
            context.startActivity(
                Intent(
                    context,
                    com.example.escaneodematerialeskof.ui.dashboard.DashboardComposeActivity::class.java
                )
            )
        },
        FunctionItem(Icons.Default.Description, "Resumen Inventario") {
            context.startActivity(
                Intent(
                    context,
                    com.example.escaneodematerialeskof.NewInventarioResumenActivity::class.java
                )
            )
        },
        FunctionItem(Icons.Default.Tune, "Más Opciones") {
            context.startActivity(
                Intent(
                    context,
                    com.example.escaneodematerialeskof.ui.opciones.MasOpcionesActivity::class.java
                )
            )
        },
        FunctionItem(Icons.Default.CompareArrows, "Comparar Tiempo Real") {
            context.startActivity(
                Intent(
                    context,
                    com.example.escaneodematerialeskof.ui.comparacion.ComparacionTiempoRealActivity::class.java
                )
            )
        },
        FunctionItem(Icons.Default.UploadFile, "Importar Datos") {
            context.startActivity(Intent(context, com.example.escaneodematerialeskof.ImportarActivity::class.java))
        },
        FunctionItem(Icons.Default.Settings, "Configuración") {
            context.startActivity(
                Intent(
                    context,
                    com.example.escaneodematerialeskof.ui.config.ConfiguracionActivity::class.java
                )
            )
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario KOF", color = Color.White) },
                backgroundColor = Color(0xFFD32F2F)
            )
        },
        backgroundColor = backgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "¡Bienvenido!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF37474F)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Coca-Cola FEMSA",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Gestión Integral de Inventario y Materiales",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = primaryTextColor
                )
                Spacer(Modifier.height(24.dp))

                // 3. Sección "Funciones Principales"
                SectionTitle("Funciones Principales")
                Spacer(Modifier.height(12.dp))

                // Grid para los 8 botones de funciones - adaptativo para diferentes tamaños de pantalla
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                ) {
                    items(mainFunctions) { item ->
                        FunctionCard(
                            item = item,
                            backgroundColor = cardBackgroundColor,
                            textColor = secondaryTextColor
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))

                // 4. Sección "Gestión Avanzada"
                SectionTitle("Gestión Avanzada")
                Spacer(Modifier.height(12.dp))

                // Botones grandes de gestión avanzada
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AdvancedButton(
                        text = "AJUSTE INVENTARIO",
                        icon = Icons.Default.Build,
                        backgroundColor = redButtonColor,
                        modifier = Modifier.weight(1f)
                    ) {
                        context.startActivity(
                            Intent(
                                context,
                                com.example.escaneodematerialeskof.AjusteInventarioActivity::class.java
                            )
                        )
                    }
                    AdvancedButton(
                        text = "GESTIÓN ALMACENES",
                        icon = Icons.Default.Store, // Usamos Store por mayor compatibilidad
                        backgroundColor = darkButtonColor,
                        modifier = Modifier.weight(1f)
                    ) {
                        context.startActivity(
                            Intent(
                                context,
                                com.example.escaneodematerialeskof.ui.almacenes.GestionAlmacenesActivity::class.java
                            )
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                // 5. Footer
                Text(
                    "Todos los módulos funcionando correctamente",
                    fontSize = 12.sp,
                    color = secondaryTextColor
                )
            }
        }
    }
}

// Composable para los títulos de cada sección
@Composable
private fun SectionTitle(title: String) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF555555),
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

// Composable para cada tarjeta de "Funciones Principales"
@Composable
private fun FunctionCard(item: FunctionItem, backgroundColor: Color, textColor: Color) {
    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = backgroundColor,
        elevation = 2.dp,
        modifier = Modifier.height(120.dp).clickable { item.onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = Color.DarkGray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = item.label,
                color = textColor,
                fontSize = 12.sp
            )
        }
    }
}

// Composable para los botones grandes de "Gestión Avanzada"
@Composable
private fun AdvancedButton(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor),
        modifier = modifier.height(60.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
