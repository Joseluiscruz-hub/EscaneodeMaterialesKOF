package com.example.escaneodematerialeskof.ui.inventario

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.escaneodematerialeskof.CapturaInventarioActivity
import com.example.escaneodematerialeskof.NewInventarioResumenActivity
import com.example.escaneodematerialeskof.RestosActivity
import com.example.escaneodematerialeskof.ui.components.FloatingCalculatorBubble
import com.example.escaneodematerialeskof.util.Constants
import java.util.*

class InventarioComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                InventarioScreen()
            }
        }
    }
}

@Composable
fun InventarioScreen() {
    val context = LocalContext.current
    val invViewModel: InventarioViewModel = viewModel()
    val uiState by invViewModel.uiState.collectAsState()
    var skuManual by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var totalPallets by remember { mutableStateOf("") }
    var restos by remember { mutableStateOf("") }
    var selectedScanMode by remember { mutableStateOf(0) }
    var selectedAlmacen by remember { mutableStateOf("Seleccionar Almacén") }
    var showAlmacenDialog by remember { mutableStateOf(false) }
    var showScanModeDialog by remember { mutableStateOf(false) }

    // Colores consistentes con MainComposeActivity
    val backgroundColor = Color(0xFFF0F2F5)
    val cardBackgroundColor = Color.White
    val primaryTextColor = Color(0xFF333333)
    val redButtonColor = Color(0xFFD32F2F)

    // Diálogo de selección de tipo de escaneo
    if (showScanModeDialog) {
        ScanModeSelectionDialog(
            onDismiss = { showScanModeDialog = false },
            onModeSelected = { mode ->
                selectedScanMode = mode
                showScanModeDialog = false
            },
            selectedMode = selectedScanMode,
            redButtonColor = redButtonColor
        )
    }

    // Diálogo de selección de almacén
    if (showAlmacenDialog) {
        AlmacenSelectionDialog(
            onDismiss = { showAlmacenDialog = false },
            onAlmacenSelected = { almacen ->
                selectedAlmacen = almacen
                showAlmacenDialog = false
            },
            selectedAlmacen = selectedAlmacen,
            redButtonColor = redButtonColor
        )
    }

    LaunchedEffect(Unit) { invViewModel.cargar(context) }
    LaunchedEffect(selectedAlmacen) {
        invViewModel.setAlmacen(selectedAlmacen)
        invViewModel.cargar(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Captura de Inventario", color = Color.White) },
                backgroundColor = redButtonColor,
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                }
            )
        },
        backgroundColor = backgroundColor
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Modo de Escaneo Activo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryTextColor,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // Barra de progreso
                item {
                    val progreso = uiState.progreso
                    Column(Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = progreso.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = redButtonColor,
                            backgroundColor = Color(0xFFE0E0E0)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Progreso capacidad: ${
                                (progreso * 100).coerceIn(0f, 100f).toInt()
                            }% de ${Constants.CAPACIDAD_PALLETS_OBJETIVO} pallets",
                            fontSize = 12.sp,
                            color = primaryTextColor,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Botón de escaneo principal
                item {
                    SectionCard(
                        title = "📱 Escáner Principal",
                        backgroundColor = cardBackgroundColor
                    ) {
                        Button(
                            onClick = {
                                // Lanzar actividad de captura con el modo seleccionado
                                val intent = Intent(context, CapturaInventarioActivity::class.java)
                                val modo = when (selectedScanMode) {
                                    1 -> "pallet"
                                    2 -> "rumba"
                                    else -> "manual"
                                }
                                intent.putExtra(CapturaInventarioActivity.EXTRA_TIPO_ESCANEO, modo)
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = redButtonColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Escanear Rápido",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Modos de escaneo
                item {
                    SectionCard(
                        title = "🔧 Modos de Escaneo",
                        backgroundColor = cardBackgroundColor
                    ) {
                        val scanModes = listOf("Scan Manual", "Scan Pallets", "Scan Rumba")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            scanModes.forEachIndexed { index, mode ->
                                Button(
                                    onClick = { selectedScanMode = index },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (selectedScanMode == index) redButtonColor else Color.LightGray,
                                        contentColor = if (selectedScanMode == index) Color.White else Color.DarkGray
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        mode,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                // Datos de entrada
                item {
                    SectionCard(
                        title = "📝 Datos de Entrada",
                        backgroundColor = cardBackgroundColor
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = skuManual,
                                onValueChange = { skuManual = it },
                                label = { Text("🏷️ SKU Manual") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = redButtonColor,
                                    focusedLabelColor = redButtonColor
                                )
                            )

                            OutlinedTextField(
                                value = ubicacion,
                                onValueChange = { ubicacion = it },
                                label = { Text("📍 Ubicación") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = redButtonColor,
                                    focusedLabelColor = redButtonColor
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = totalPallets,
                                    onValueChange = { totalPallets = it },
                                    label = { Text("📦 Total Pallets") },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = redButtonColor,
                                        focusedLabelColor = redButtonColor
                                    )
                                )

                                OutlinedTextField(
                                    value = restos,
                                    onValueChange = { restos = it },
                                    label = { Text("📊 Restos") },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = redButtonColor,
                                        focusedLabelColor = redButtonColor
                                    )
                                )
                            }
                        }
                    }
                }

                // Selección de almacén
                item {
                    SectionCard(
                        title = "🏢 Configuración de Almacén",
                        backgroundColor = cardBackgroundColor
                    ) {
                        Button(
                            onClick = { showAlmacenDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (selectedAlmacen != "Seleccionar Almacén") Color(0xFF4CAF50) else Color.Gray,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Store, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text(selectedAlmacen, fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Configuración de escaneo
                item {
                    SectionCard(
                        title = "⚙️ Configuración de Escaneo",
                        backgroundColor = cardBackgroundColor
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showScanModeDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = redButtonColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Configurar Modo de Escaneo", fontSize = 16.sp)
                                }
                            }

                            // Mostrar modo actual seleccionado
                            val scanModes = listOf("Scan Manual", "Scan Pallets", "Scan Rumba")
                            Text(
                                "Modo actual: ${scanModes[selectedScanMode]}",
                                fontSize = 14.sp,
                                color = primaryTextColor,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Acciones principales
                item {
                    SectionCard(
                        title = "⚡ Acciones Principales",
                        backgroundColor = cardBackgroundColor
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ActionButton(
                                    text = "Exportar",
                                    icon = Icons.Default.FileDownload,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Ir a resumen donde se puede exportar
                                    context.startActivity(Intent(context, NewInventarioResumenActivity::class.java))
                                }

                                ActionButton(
                                    text = "Enviar Correo",
                                    icon = Icons.Default.Email,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Reutilizar la lógica de Captura para enviar correo
                                    val intent = Intent(context, CapturaInventarioActivity::class.java)
                                    intent.putExtra("enviar_por_correo", true)
                                    context.startActivity(intent)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ActionButton(
                                    text = "Resumen",
                                    icon = Icons.Default.Description,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    context.startActivity(Intent(context, NewInventarioResumenActivity::class.java))
                                }

                                ActionButton(
                                    text = "Cargar",
                                    icon = Icons.Default.Upload,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Abrir la captura para usar su flujo de importación
                                    context.startActivity(Intent(context, CapturaInventarioActivity::class.java))
                                }
                            }
                        }
                    }
                }

                // Resumen final
                item {
                    SectionCard(
                        title = "📈 Resumen",
                        backgroundColor = cardBackgroundColor
                    ) {
                        if (uiState.cargando) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                CircularProgressIndicator(color = redButtonColor)
                            }
                        } else {
                            if (uiState.error != null) {
                                Text(
                                    text = "Error: ${uiState.error}",
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // KPIs
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    KpiCard(
                                        "Pallets",
                                        uiState.totalPallets.toString(),
                                        Color(0xFF2E7D32),
                                        Modifier.weight(1f)
                                    )
                                    KpiCard(
                                        "Cajas",
                                        uiState.totalCajas.toString(),
                                        Color(0xFF1565C0),
                                        Modifier.weight(1f)
                                    )
                                    KpiCard(
                                        "SKUs",
                                        uiState.totalSkus.toString(),
                                        Color(0xFFF57C00),
                                        Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    KpiCard(
                                        "Ubicaciones",
                                        uiState.ubicacionesDistintas.toString(),
                                        Color(0xFF6A1B9A),
                                        Modifier.weight(1f)
                                    )
                                    KpiCard(
                                        "Prom Pal/SKU",
                                        String.format(Locale.getDefault(), "%.1f", uiState.promedioPalletsPorSku),
                                        Color(0xFF00897B),
                                        Modifier.weight(1f)
                                    )
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = Color(0xFFE8F5E8),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "Último SKU: ${uiState.ultimoSku.ifBlank { "-" }}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Text(
                                            "Pallets: ${uiState.ultimoPallets}  Ubicación: ${uiState.ultimoUbicacion.ifBlank { "-" }}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Actualizado: ${uiState.ultimaActualizacion}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                if (uiState.ultimosEscaneos.isNotEmpty()) {
                                    Text(
                                        "Últimos Escaneos",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryTextColor
                                    )
                                    Column(Modifier.fillMaxWidth()) {
                                        uiState.ultimosEscaneos.forEach { escaneo ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(escaneo.sku, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                Text(
                                                    "${escaneo.pallets} pal · ${escaneo.ubicacion}",
                                                    fontSize = 11.sp,
                                                    color = Color.DarkGray
                                                )
                                            }
                                            Divider()
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { invViewModel.cargar(context) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Refrescar", color = Color.White)
                                    }
                                    Button(
                                        onClick = {
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    NewInventarioResumenActivity::class.java
                                                )
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1976D2)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Filled.Assessment, contentDescription = null, tint = Color.White)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Reportes", color = Color.White)
                                    }
                                }
                                Button(
                                    onClick = {
                                        context.startActivity(Intent(context, NewInventarioResumenActivity::class.java))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CompareArrows, contentDescription = null, tint = Color.White)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Comparar y Enviar", color = Color.White, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Datos escaneados (información del QR)
                item {
                    SectionCard(
                        title = "📊 Datos Escaneados",
                        backgroundColor = cardBackgroundColor
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    label = { Text("🏷️ SKU") },
                                    enabled = false,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        disabledBorderColor = Color.LightGray,
                                        disabledTextColor = Color.DarkGray
                                    )
                                )

                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    label = { Text("📦 DP") },
                                    enabled = false,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        disabledBorderColor = Color.LightGray,
                                        disabledTextColor = Color.DarkGray
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    label = { Text("📦 CxPal") },
                                    enabled = false,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        disabledBorderColor = Color.LightGray,
                                        disabledTextColor = Color.DarkGray
                                    )
                                )

                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    label = { Text("📅 FPC") },
                                    enabled = false,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        disabledBorderColor = Color.LightGray,
                                        disabledTextColor = Color.DarkGray
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    label = { Text("🔗 Con") },
                                    enabled = false,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        disabledBorderColor = Color.LightGray,
                                        disabledTextColor = Color.DarkGray
                                    )
                                )

                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    label = { Text("🏢 Centro") },
                                    enabled = false,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        disabledBorderColor = Color.LightGray,
                                        disabledTextColor = Color.DarkGray
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    label = { Text("📋 LINEA") },
                                    enabled = false,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        disabledBorderColor = Color.LightGray,
                                        disabledTextColor = Color.DarkGray
                                    )
                                )

                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    label = { Text("⚙️ OP") },
                                    enabled = false,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        disabledBorderColor = Color.LightGray,
                                        disabledTextColor = Color.DarkGray
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    label = { Text("📅 FProd") },
                                    enabled = false,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        disabledBorderColor = Color.LightGray,
                                        disabledTextColor = Color.DarkGray
                                    )
                                )

                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    label = { Text("📆 DiasV") },
                                    enabled = false,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        disabledBorderColor = Color.LightGray,
                                        disabledTextColor = Color.DarkGray
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    label = { Text("🏷️ Lote") },
                                    enabled = false,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        disabledBorderColor = Color.LightGray,
                                        disabledTextColor = Color.DarkGray
                                    )
                                )

                                OutlinedTextField(
                                    value = "",
                                    onValueChange = { },
                                    label = { Text("🔄 Tipo Movimiento") },
                                    enabled = false,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        disabledBorderColor = Color.LightGray,
                                        disabledTextColor = Color.DarkGray
                                    )
                                )
                            }
                        }
                    }
                }

                // Gestión de datos
                item {
                    SectionCard(
                        title = "💾 Gestión de Datos",
                        backgroundColor = cardBackgroundColor
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActionButton(
                                text = "Restos",
                                icon = Icons.Default.Calculate,
                                modifier = Modifier.weight(1f)
                            ) {
                                context.startActivity(Intent(context, RestosActivity::class.java))
                            }

                            Button(
                                onClick = {
                                    // Guardar (delegar a actividad clásica de captura, que maneja persistencia actual)
                                    val intent = Intent(context, CapturaInventarioActivity::class.java)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Guardar", color = Color.White, fontSize = 12.sp)
                                }
                            }

                            ActionButton(
                                text = "Siguiente",
                                icon = Icons.Filled.NavigateNext,
                                modifier = Modifier.weight(1f)
                            ) {
                                Toast.makeText(context, "Siguiente elemento...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            FloatingCalculatorBubble(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    backgroundColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7D0606),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF666666)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ScanModeSelectionDialog(
    onDismiss: () -> Unit,
    onModeSelected: (Int) -> Unit,
    selectedMode: Int,
    redButtonColor: Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Seleccionar Modo de Escaneo",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    "Seleccione el modo de escaneo deseado:",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val modes = listOf(
                    "Scan Manual" to "Escaneo manual de códigos QR",
                    "Scan Pallets" to "Escaneo automático de pallets completos",
                    "Scan Rumba" to "Escaneo rápido con validación automática"
                )

                modes.forEachIndexed { index, (mode, description) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onModeSelected(index) },
                        backgroundColor = if (selectedMode == index) redButtonColor.copy(alpha = 0.1f) else Color.White,
                        elevation = 2.dp,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMode == index,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = redButtonColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    mode,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedMode == index) redButtonColor else Color.Black
                                )
                                Text(
                                    description,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar", color = Color.Gray)
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(backgroundColor = redButtonColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aceptar", color = Color.White)
                }
            }
        }
    )
}

@Composable
fun AlmacenSelectionDialog(
    onDismiss: () -> Unit,
    onAlmacenSelected: (String) -> Unit,
    selectedAlmacen: String,
    redButtonColor: Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Seleccionar Almacén",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    "Seleccione el almacén donde realizará el inventario:",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val almacenes = listOf(
                    "Almacén Central A" to "Productos terminados - Zona A",
                    "Almacén Central B" to "Productos terminados - Zona B",
                    "Almacén Materias Primas" to "Insumos y materias primas",
                    "Almacén Temporal" to "Productos en tránsito",
                    "Almacén Devoluciones" to "Productos devueltos"
                )

                almacenes.forEach { (almacen, description) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onAlmacenSelected(almacen) },
                        backgroundColor = if (selectedAlmacen == almacen) redButtonColor.copy(alpha = 0.1f) else Color.White,
                        elevation = 2.dp,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAlmacen == almacen,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = redButtonColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    almacen,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedAlmacen == almacen) redButtonColor else Color.Black
                                )
                                Text(
                                    description,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar", color = Color.Gray)
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(backgroundColor = redButtonColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aceptar", color = Color.White)
                }
            }
        }
    )
}

@Composable
fun KpiCard(titulo: String, valor: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(10.dp),
        backgroundColor = color.copy(alpha = 0.1f),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(valor, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Text(titulo, fontSize = 11.sp, color = color.darken())
        }
    }
}

private fun Color.darken(factor: Float = 0.75f): Color = Color(red * factor, green * factor, blue * factor, alpha)
