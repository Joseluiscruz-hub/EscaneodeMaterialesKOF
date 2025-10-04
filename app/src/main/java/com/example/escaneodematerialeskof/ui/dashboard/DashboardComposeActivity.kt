package com.example.escaneodematerialeskof.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.escaneodematerialeskof.AjusteInventarioActivity
import com.example.escaneodematerialeskof.NewInventarioResumenActivity
import com.example.escaneodematerialeskof.ui.components.FloatingCalculatorBubble
import java.util.*

class DashboardComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DashboardScreen()
            }
        }
    }
}

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val viewModel: DashboardEjecutivoViewModel = viewModel()

    // Colores consistentes
    val backgroundColor = Color(0xFFF0F2F5)
    val cardBackgroundColor = Color.White
    val primaryTextColor = Color(0xFF333333)
    val redButtonColor = Color(0xFFD32F2F)
    val greenColor = Color(0xFF4CAF50)
    val blueColor = Color(0xFF2196F3)
    val orangeColor = Color(0xFFFF9800)

    // Observar el estado del dashboard
    val dashboardState by viewModel.dashboardState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    // Estados UI para filtros
    var almacenExpanded by remember { mutableStateOf(false) }
    var tarimaExpanded by remember { mutableStateOf(false) }
    var selectedAlmacen by remember(dashboardState.filtroAlmacen, dashboardState.almacenesDisponibles) {
        mutableStateOf(dashboardState.filtroAlmacen ?: "Todos")
    }
    var selectedTarima by remember(dashboardState.filtroTarima, dashboardState.tarimasDisponibles) {
        mutableStateOf(dashboardState.filtroTarima ?: "Todos")
    }
    var intervalExpanded by remember { mutableStateOf(false) }
    val intervalOptions = listOf(5_000L, 10_000L, 30_000L, 60_000L)

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        viewModel.cargarDatosDashboard(context)
        viewModel.startRealtimeMonitoring(context)
    }

    // Reaplicar filtros cuando cambien selecciones
    LaunchedEffect(selectedAlmacen, selectedTarima) {
        viewModel.setFilters(
            almacen = selectedAlmacen.takeIf { it != "Todos" },
            tarima = selectedTarima.takeIf { it != "Todos" }
        )
        viewModel.cargarDatosDashboard(context)
    }

    // Manejar el refresco
    if (isRefreshing) {
        LaunchedEffect(Unit) {
            viewModel.cargarDatosDashboard(context)
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Ejecutivo", color = Color.White) },
                backgroundColor = redButtonColor,
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { isRefreshing = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                    }
                }
            )
        },
        backgroundColor = backgroundColor
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) { // Contenedor para superponer la burbuja
            if (dashboardState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = redButtonColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Filtros
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = cardBackgroundColor
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    "Filtros",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Almacén
                                    Box(Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { almacenExpanded = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text(selectedAlmacen, maxLines = 1) }
                                        DropdownMenu(
                                            expanded = almacenExpanded,
                                            onDismissRequest = { almacenExpanded = false }) {
                                            DropdownMenuItem(onClick = {
                                                selectedAlmacen = "Todos"; almacenExpanded = false
                                            }) { Text("Todos") }
                                            dashboardState.almacenesDisponibles.forEach { alm ->
                                                DropdownMenuItem(onClick = {
                                                    selectedAlmacen = alm; almacenExpanded = false
                                                }) { Text(alm.take(24)) }
                                            }
                                        }
                                    }
                                    // Tarima
                                    Box(Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { tarimaExpanded = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text(selectedTarima, maxLines = 1) }
                                        DropdownMenu(
                                            expanded = tarimaExpanded,
                                            onDismissRequest = { tarimaExpanded = false }) {
                                            DropdownMenuItem(onClick = {
                                                selectedTarima = "Todos"; tarimaExpanded = false
                                            }) { Text("Todos") }
                                            dashboardState.tarimasDisponibles.forEach { t ->
                                                DropdownMenuItem(onClick = {
                                                    selectedTarima = t; tarimaExpanded = false
                                                }) { Text(t.take(24)) }
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                // Intervalo
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Intervalo monitoreo:", fontSize = 12.sp, color = Color.Gray)
                                    Spacer(Modifier.width(8.dp))
                                    Box {
                                        OutlinedButton(onClick = { intervalExpanded = true }) {
                                            Text(
                                                text = "${dashboardState.monitoringIntervalMs / 1000}s",
                                                fontSize = 12.sp
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = intervalExpanded,
                                            onDismissRequest = { intervalExpanded = false }) {
                                            intervalOptions.forEach { opt ->
                                                DropdownMenuItem(onClick = {
                                                    intervalExpanded = false
                                                    viewModel.updateMonitoringInterval(context, opt)
                                                }) { Text("${opt / 1000}s") }
                                            }
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    if (dashboardState.nuevasAlertasCount > 0) {
                                        Text(
                                            "Nuevas alertas: ${dashboardState.nuevasAlertasCount}",
                                            fontSize = 12.sp,
                                            color = redButtonColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    // Historial (Sparkline)
                    if (dashboardState.historialPallets.size > 1) {
                        item {
                            HistorySection(
                                historial = dashboardState.historialPallets,
                                cardBackgroundColor = cardBackgroundColor,
                                primaryTextColor = primaryTextColor,
                                accentColor = blueColor
                            )
                        }
                    }

                    // Encabezado de bienvenida
                    item {
                        WelcomeHeader(
                            greenColor = greenColor,
                            cardBackgroundColor = cardBackgroundColor,
                            primaryTextColor = primaryTextColor,
                            ultimaActualizacion = dashboardState.ultimaActualizacion
                        )
                    }

                    // Métricas principales
                    item {
                        Text(
                            "📊 Métricas Principales",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor
                        )
                    }

                    // Tendencias
                    item {
                        if (dashboardState.tendenciaTotalPallets != null || dashboardState.tendenciaTotalSkus != null) {
                            TrendSection(dashboardState, cardBackgroundColor)
                        }
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                listOf(
                                    MetricCard(
                                        "Total SKUs",
                                        dashboardState.totalSKUs.toString(),
                                        "📦",
                                        greenColor
                                    ),
                                    MetricCard(
                                        "Total Pallets",
                                        dashboardState.totalPallets.toString(),
                                        "🏗️",
                                        blueColor
                                    ),
                                    MetricCard(
                                        "Almacenes",
                                        dashboardState.almacenesActivos.toString(),
                                        "🏢",
                                        orangeColor
                                    ),
                                    MetricCard(
                                        "Alertas",
                                        dashboardState.alertasCriticas.toString(),
                                        "⚠️",
                                        if (dashboardState.alertasCriticas > 0) redButtonColor else greenColor
                                    )
                                )
                            ) { metric ->
                                MetricCardView(metric)
                            }
                        }
                    }

                    // Resumen por almacén (datos reales)
                    if (dashboardState.resumenAlmacenes.isNotEmpty()) {
                        item {
                            Text(
                                "🏢 Resumen por Almacén",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                        }

                        item {
                            ResumenAlmacenesCard(dashboardState.resumenAlmacenes, cardBackgroundColor)
                        }
                    }

                    // Resumen por tipo de tarima
                    if (dashboardState.resumenTiposTarima.isNotEmpty()) {
                        item {
                            Text(
                                "📦 Tipos de Tarimas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                        }

                        item {
                            ResumenTarimasCard(
                                dashboardState.resumenTiposTarima,
                                dashboardState.totalPallets,
                                cardBackgroundColor,
                                blueColor
                            )
                        }
                    }

                    // Alertas inteligentes
                    if (dashboardState.alertas.isNotEmpty()) {
                        item {
                            Text(
                                "🚨 Alertas del Sistema",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                        }

                        item {
                            AlertasCard(dashboardState.alertas, cardBackgroundColor, orangeColor)
                        }
                    }

                    // Top 5 SKUs con más pallets
                    if (dashboardState.topSKUs.isNotEmpty()) {
                        item {
                            Text(
                                "🏆 Top 5 SKUs",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                        }

                        item {
                            TopSKUsCard(dashboardState.topSKUs, cardBackgroundColor, blueColor)
                        }
                    }

                    // Acciones rápidas
                    item {
                        Text(
                            "⚡ Acciones Rápidas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionButton(
                                text = "Nuevo\nInventario",
                                icon = Icons.Default.Add,
                                color = greenColor,
                                modifier = Modifier.weight(1f)
                            ) {
                                val intent = Intent(context, AjusteInventarioActivity::class.java)
                                context.startActivity(intent)
                            }

                            QuickActionButton(
                                text = "Ver\nReportes",
                                icon = Icons.Default.Assessment,
                                color = blueColor,
                                modifier = Modifier.weight(1f)
                            ) {
                                val intent = Intent(context, NewInventarioResumenActivity::class.java)
                                context.startActivity(intent)
                            }

                            QuickActionButton(
                                text = "Comparar\nInventario",
                                icon = Icons.Default.Compare,
                                color = orangeColor,
                                modifier = Modifier.weight(1f)
                            ) {
                                val intent = Intent(context, NewInventarioResumenActivity::class.java)
                                context.startActivity(intent)
                            }
                        }
                    }

                    // Estadísticas generales
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = cardBackgroundColor,
                            shape = RoundedCornerShape(12.dp),
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "📈 Resumen General",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatisticItem(
                                        dashboardState.totalSKUs.toString(),
                                        "SKUs Únicos",
                                        greenColor
                                    )
                                    StatisticItem(
                                        dashboardState.totalPallets.toString(),
                                        "Total Pallets",
                                        blueColor
                                    )
                                    StatisticItem(
                                        String.format("%.1f%%", dashboardState.tasaOcupacion),
                                        "Ocupación",
                                        orangeColor
                                    )
                                }
                            }
                        }
                    }

                    // Distribución porcentual tarimas
                    if (dashboardState.distribucionTarimasPct.isNotEmpty()) {
                        item {
                            DistributionSection(
                                title = "📦 Distribución Tarimas (%)",
                                data = dashboardState.distribucionTarimasPct,
                                cardBackgroundColor = cardBackgroundColor,
                                barColor = blueColor
                            )
                        }
                    }
                    // Distribución porcentual almacenes
                    if (dashboardState.distribucionAlmacenesPct.isNotEmpty()) {
                        item {
                            DistributionSection(
                                title = "🏢 Distribución Almacenes (%)",
                                data = dashboardState.distribucionAlmacenesPct,
                                cardBackgroundColor = cardBackgroundColor,
                                barColor = orangeColor
                            )
                        }
                    }
                    // Alertas en tiempo real
                    if (dashboardState.realtimeAlerts.isNotEmpty()) {
                        item {
                            RealtimeAlertsSection(
                                nuevas = dashboardState.realtimeAlerts,
                                cardBackgroundColor = cardBackgroundColor,
                                redButtonColor = redButtonColor
                            )
                        }
                    }

                    // Espacio final
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            FloatingCalculatorBubble( // Burbuja flotante sobre el contenido
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

data class MetricCard(val title: String, val value: String, val icon: String, val color: Color)

@Composable
fun ResumenAlmacenesCard(resumenAlmacenes: List<AlmacenData>, cardBackgroundColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = cardBackgroundColor,
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            resumenAlmacenes.forEachIndexed { index, almacen ->
                AlmacenRow(almacen)
                if (index != resumenAlmacenes.lastIndex) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun ResumenTarimasCard(
    resumenTiposTarima: Map<String, Int>,
    totalPallets: Int,
    cardBackgroundColor: Color,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = cardBackgroundColor,
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            resumenTiposTarima.forEach { (tipo, cantidad) ->
                TipoTarimaRow(
                    tipo = tipo,
                    cantidad = cantidad,
                    total = totalPallets,
                    color = color
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AlertasCard(alertas: List<AlertaInteligente>, cardBackgroundColor: Color, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = cardBackgroundColor,
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            alertas.take(5).forEach { alerta ->
                AlertRow(alerta, color)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TopSKUsCard(topSKUs: List<TopSKUData>, cardBackgroundColor: Color, blueColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = cardBackgroundColor,
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            topSKUs.forEachIndexed { index, sku ->
                TopSKURow(
                    posicion = index + 1,
                    sku = sku.sku,
                    descripcion = sku.descripcion,
                    pallets = sku.pallets,
                    color = when (index) {
                        0 -> Color(0xFFFFD700)
                        1 -> Color(0xFFC0C0C0)
                        2 -> Color(0xFFCD7F32)
                        else -> blueColor
                    }
                )
                if (index != topSKUs.lastIndex) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun MetricCardView(metric: MetricCard) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                metric.icon,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                metric.value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = metric.color
            )
            Text(
                metric.title,
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AlmacenRow(almacen: AlmacenData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                almacen.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Text(
                "${almacen.items} elementos",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${almacen.percentage}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = almacen.statusColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(almacen.statusColor, shape = RoundedCornerShape(6.dp))
            )
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(80.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text,
                color = Color.White,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatisticItem(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WelcomeHeader(
    greenColor: Color,
    cardBackgroundColor: Color,
    primaryTextColor: Color,
    ultimaActualizacion: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = cardBackgroundColor,
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Dashboard Ejecutivo",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Resumen General del Sistema de Inventarios",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = greenColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Sistema Operativo",
                    fontSize = 12.sp,
                    color = greenColor
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Última Actualización: $ultimaActualizacion",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TipoTarimaRow(tipo: String, cantidad: Int, total: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            tipo,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333),
            modifier = Modifier.weight(1f)
        )
        Text(
            "$cantidad pallets",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(100.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun AlertRow(alerta: AlertaInteligente, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                alerta.mensaje,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Text(
                "Ubicación: ${alerta.ubicacion}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text(
            alerta.fechaHora,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun TopSKURow(posicion: Int, sku: String, descripcion: String, pallets: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "#$posicion",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(40.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                sku,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Text(
                descripcion,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text(
            "$pallets pallets",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(100.dp),
            textAlign = TextAlign.End
        )
    }
}

// Sustitución de TrendSection y TrendBadge antiguos
@Composable
private fun TrendSection(state: DashboardState, cardBackgroundColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = cardBackgroundColor,
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("📈 Tendencias", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                state.tendenciaTotalPallets?.let { pct ->
                    TrendBadge(
                        label = "Pallets",
                        value = pct,
                        positiveColor = Color(0xFF2E7D32),
                        negativeColor = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                }
                state.tendenciaTotalSkus?.let { pct ->
                    TrendBadge(
                        label = "SKUs",
                        value = pct,
                        positiveColor = Color(0xFF1565C0),
                        negativeColor = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendBadge(
    label: String,
    value: Double,
    positiveColor: Color,
    negativeColor: Color,
    modifier: Modifier = Modifier
) {
    val color = if (value >= 0) positiveColor else negativeColor
    val arrow = if (value >= 0) "▲" else "▼"
    Card(
        modifier = modifier.height(80.dp),
        backgroundColor = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(10.dp),
        elevation = 2.dp
    ) {
        Column(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.Center) {
            Text(label, fontSize = 12.sp, color = color)
            Text(
                "$arrow ${String.format(Locale.getDefault(), "%.1f%%", value)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun DistributionSection(
    title: String,
    data: Map<String, Double>,
    cardBackgroundColor: Color,
    barColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = cardBackgroundColor,
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            data.entries.sortedByDescending { it.value }.forEach { (k, v) ->
                Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(k.take(20), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(String.format(Locale.getDefault(), "%.1f%%", v), fontSize = 12.sp, color = barColor)
                    }
                    LinearProgressIndicator(
                        progress = (v / 100.0).toFloat().coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = barColor,
                        backgroundColor = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    }
}

@Composable
private fun RealtimeAlertsSection(
    nuevas: List<AlertaInteligente>,
    cardBackgroundColor: Color,
    redButtonColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = cardBackgroundColor,
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("⏱️ Alertas Recientes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            nuevas.take(5).forEach { alerta ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = null,
                        tint = redButtonColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(alerta.mensaje, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(alerta.ubicacion, fontSize = 10.sp, color = Color.Gray)
                    }
                    Text(alerta.fechaHora, fontSize = 10.sp, color = Color.Gray)
                }
                Divider()
            }
            if (nuevas.isEmpty()) {
                Text("Sin alertas nuevas", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun HistorySection(
    historial: List<Int>,
    cardBackgroundColor: Color,
    primaryTextColor: Color,
    accentColor: Color
) {
    val maxValue = (historial.maxOrNull() ?: 1).coerceAtLeast(1)
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = cardBackgroundColor,
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("📉 Historial Pallets", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.Bottom
            ) {
                historial.forEach { value ->
                    val h = (value.toFloat() / maxValue) * 50f + 4f
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(h.dp)
                            .padding(horizontal = 1.dp)
                            .background(accentColor.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pts: ${historial.size}", fontSize = 10.sp, color = Color.Gray)
                Text("Max: $maxValue", fontSize = 10.sp, color = Color.Gray)
                Text("Últ: ${historial.last()}", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}
