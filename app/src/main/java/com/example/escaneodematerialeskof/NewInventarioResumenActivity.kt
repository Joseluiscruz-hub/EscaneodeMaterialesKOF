package com.example.escaneodematerialeskof

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.escaneodematerialeskof.manager.AlmacenCapacidadManager
import com.example.escaneodematerialeskof.model.AlmacenCapacidad
import com.example.escaneodematerialeskof.model.MaterialItem
import com.example.escaneodematerialeskof.ui.comparacion.ComparacionTiempoRealActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NewInventarioResumenActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // Elementos principales
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    // Elementos de la interfaz
    private lateinit var tvTotalItems: TextView
    private lateinit var tvTotalPallets: TextView
    private lateinit var lvRecentItems: ListView
    private lateinit var btnExportSummary: Button
    private lateinit var btnRefresh: Button
    private lateinit var btnViewDetails: Button
    private lateinit var pieChart: PieChart
    private lateinit var spinnerCentro: Spinner
    private lateinit var btnImportInventory: Button
    private lateinit var btnClearData: Button

    // Elementos del Dashboard mejorado
    private lateinit var tvPorcentajeCompletitud: TextView
    private lateinit var tvUltimaActualizacion: TextView
    private lateinit var tvDiscrepanciasCriticas: TextView
    private lateinit var layoutAlertas: LinearLayout
    private lateinit var btnComparacionRapida: Button
    private lateinit var btnVerTendencias: Button
    private lateinit var pieChartTipoTarima: PieChart

    private lateinit var viewModel: CapturaInventarioViewModel
    private var centroSeleccionado: String? = null
    private var skuSeleccionado: String? = null
    private var listaMateriales: List<MaterialItem> = emptyList()
    private var mostrarDetalles: Boolean = false

    // Para importar archivos
    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.importarInventario(it) { success, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                if (success) {
                    actualizarResumen()
                    actualizarSpinnerSKU()
                }
            }
        }
    }

    // Para exportar archivos
    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        uri?.let {
            viewModel.exportarInventario(it) { success, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private lateinit var almacenManager: AlmacenCapacidadManager
    private var almacenSeleccionado: AlmacenCapacidad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario_resumen)

        initializeViews()
        setupDrawer()
        setupClickListeners()
        initializeViewModel()

        almacenManager = AlmacenCapacidadManager(this)
        mostrarDialogoSeleccionAlmacen()
    }

    private fun initializeViews() {
        // DrawerLayout y NavigationView
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        // Elementos principales
        tvTotalItems = findViewById(R.id.tvTotalItems)
        tvTotalPallets = findViewById(R.id.tvTotalPallets)
        lvRecentItems = findViewById(R.id.lvRecentItems)
        btnExportSummary = findViewById(R.id.btnExportSummary)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnViewDetails = findViewById(R.id.btnViewDetails)
        pieChart = findViewById(R.id.pieChart)
        spinnerCentro = findViewById(R.id.spinnerCentro)
        btnImportInventory = findViewById(R.id.btnImportInventory)
        btnClearData = findViewById(R.id.btnClearData)

        // Elementos del Dashboard mejorado
        tvPorcentajeCompletitud = findViewById(R.id.tvPorcentajeCompletitud)
        tvUltimaActualizacion = findViewById(R.id.tvUltimaActualizacion)
        tvDiscrepanciasCriticas = findViewById(R.id.tvDiscrepanciasCriticas)
        layoutAlertas = findViewById(R.id.layoutAlertas)
        btnComparacionRapida = findViewById(R.id.btnComparacionRapida)
        btnVerTendencias = findViewById(R.id.btnVerTendencias)
        pieChartTipoTarima = findViewById(R.id.pieChartTipoTarima)

        // Configurar toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun setupDrawer() {
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, findViewById(R.id.toolbar),
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun setupClickListeners() {
        btnRefresh.setOnClickListener {
            actualizarResumen()
            Toast.makeText(this, "🔄 Datos actualizados", Toast.LENGTH_SHORT).show()
        }

        btnExportSummary.setOnClickListener {
            exportarResumen()
        }

        btnViewDetails.setOnClickListener {
            mostrarDetalles = !mostrarDetalles
            if (mostrarDetalles) {
                btnViewDetails.text = "📋 Ocultar Detalles"
                mostrarDetallesCompletos()
            } else {
                btnViewDetails.text = "📋 Ver Detalles"
                actualizarResumen()
            }
        }

        btnImportInventory.setOnClickListener {
            importLauncher.launch("*/*")
        }

        btnClearData.setOnClickListener {
            mostrarDialogoLimpiarDatos()
        }

        btnComparacionRapida.setOnClickListener {
            val intent = Intent(this, ComparacionTiempoRealActivity::class.java)
            startActivity(intent)
        }

        btnVerTendencias.setOnClickListener {
            mostrarTendencias()
        }

        spinnerCentro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                centroSeleccionado = if (position == 0) null else parent?.getItemAtPosition(position) as String
                actualizarResumen()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                centroSeleccionado = null
            }
        }
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[CapturaInventarioViewModel::class.java]
    }

    private fun actualizarSpinnerSKU() {
        // Usar los materiales filtrados por almacén para actualizar el spinner
        val lista = getMaterialesFiltradosPorAlmacen()
        val skus = lista.map { it.sku }.distinct().sorted().toMutableList()
        skus.add(0, "Todos")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, skus)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCentro.adapter = adapter
        
        // Mantener la selección actual si es posible
        if (skuSeleccionado != null && skuSeleccionado != "Todos" && skus.contains(skuSeleccionado)) {
            val position = skus.indexOf(skuSeleccionado)
            if (position >= 0) {
                spinnerCentro.setSelection(position)
            }
        } else {
            // Seleccionar "Todos" por defecto
            spinnerCentro.setSelection(0)
        }
    }

    private fun getMaterialesFiltradosPorAlmacen(): List<MaterialItem> {
        val nombreAlmacen = almacenSeleccionado?.nombreAlmacen
        // Si no hay almacén seleccionado, devolver todos los materiales
        if (nombreAlmacen.isNullOrEmpty()) {
            return viewModel.obtenerMaterialesEscaneados()
        }
        // Filtrar por almacén, considerando que el campo almacen puede ser null o vacío
        return viewModel.obtenerMaterialesEscaneados().filter { 
            it.almacen == nombreAlmacen || (nombreAlmacen == "Todos" && !it.almacen.isNullOrEmpty())
        }
    }

    private fun actualizarResumen() {
        val lista = getMaterialesFiltradosPorAlmacen()
        val listaFiltrada = if (skuSeleccionado.isNullOrEmpty() || skuSeleccionado == "Todos") lista else lista.filter { it.sku == skuSeleccionado }
        tvTotalItems.text = listaFiltrada.size.toString()
        val totalPallets = listaFiltrada.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
        tvTotalPallets.text = totalPallets.toString()

        val detalles = if (mostrarDetalles) {
            listaFiltrada.takeLast(10).map {
                """
                SKU: ${it.sku}
                Descripción: ${it.descripcion}
                Centro: ${it.centro}
                Línea: ${it.linea}
                OP: ${it.op}
                F. Producción: ${it.fProd}
                Días V: ${it.diasV}
                Ubicación: ${it.ubicacion}
                Pallets: ${it.totalPallets}
                Restos: ${it.restos}
                Tipo Tarima: ${it.tipoTarima}
                """.trimIndent()
            }
        } else {
            listaFiltrada.takeLast(10).map {
                "SKU: ${it.sku}\nDesc: ${it.descripcion}\nPallets: ${it.totalPallets ?: "-"}"
            }
        }

        lvRecentItems.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, detalles)
        mostrarGraficoPorSKU(listaFiltrada)
        mostrarGraficoTipoTarima(listaFiltrada)

        lvRecentItems.setOnItemClickListener { _, _, position, _ ->
            val item = listaFiltrada.takeLast(10)[position]
            val detalles = """
                SKU: ${item.sku}
                Descripción: ${item.descripcion}
                Centro: ${item.centro}
                Línea: ${item.linea}
                OP: ${item.op}
                F. Producción: ${item.fProd}
                Días V: ${item.diasV}
                Ubicación: ${item.ubicacion}
                Pallets: ${item.totalPallets}
                Restos: ${item.restos}
                Tipo Tarima: ${item.tipoTarima}
            """.trimIndent()
            AlertDialog.Builder(this)
                .setTitle("Detalle del Item")
                .setMessage(detalles)
                .setPositiveButton("Cerrar", null)
                .show()
        }

        // Comentar temporalmente elementos del Dashboard mejorado hasta actualizar el layout
        // val totalItems = lista.size
        // val totalPalletsImportados = lista.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
        // val totalPalletsEsperados = viewModel.obtenerTotalPalletsEsperados()
        // val porcentajeCompletitud = if (totalPalletsEsperados > 0) (totalPalletsImportados.toDouble() / totalPalletsEsperados) * 100 else 0.0
        // tvPorcentajeCompletitud.text = String.format("%.1f%%", porcentajeCompletitud)
        // tvUltimaActualizacion.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        // tvDiscrepanciasCriticas.text = viewModel.obtenerDiscrepanciasCriticas().toString()

        // Mostrar u ocultar layout de alertas
        // val hayDiscrepancias = viewModel.obtenerDiscrepanciasCriticas() > 0
        // layoutAlertas.visibility = if (hayDiscrepancias) View.VISIBLE else View.GONE

        // Actualizar gráfico de tipo de tarima
        // mostrarGraficoTipoTarima(lista)
    }

    private fun mostrarGraficoPorSKU(lista: List<MaterialItem>) {
        val coloresPersonalizados = listOf(
            android.graphics.Color.parseColor("#1976D2"),
            android.graphics.Color.parseColor("#E53935"),
            android.graphics.Color.parseColor("#43A047"),
            android.graphics.Color.parseColor("#FBC02D"),
            android.graphics.Color.parseColor("#8E24AA"),
            android.graphics.Color.parseColor("#00838F"),
            android.graphics.Color.parseColor("#F57C00"),
            android.graphics.Color.parseColor("#6D4C41")
        )
        val conteoPorSKU = lista.groupBy { it.sku }.mapValues { (_, items) ->
            items.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
        }
        val entries = conteoPorSKU.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(entries, "SKU")
        dataSet.colors = coloresPersonalizados
        dataSet.valueTextSize = 14f
        dataSet.valueFormatter = PercentFormatter(pieChart)
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 8f
        val data = PieData(dataSet)
        data.setValueTextSize(14f)
        data.setValueTextColor(android.graphics.Color.WHITE)
        pieChart.data = data
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.centerText = "SKU"
        pieChart.setEntryLabelColor(android.graphics.Color.BLACK)
        pieChart.legend.isEnabled = true
        pieChart.legend.textSize = 14f
        pieChart.legend.isWordWrapEnabled = true
        pieChart.animateY(1000)
        pieChart.invalidate()
        pieChart.setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
            override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                if (e is PieEntry) {
                    val porcentaje = String.format(Locale.getDefault(), "%.1f", e.value)
                    Toast.makeText(this@NewInventarioResumenActivity, "SKU: ${e.label}\nCantidad: $porcentaje", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onNothingSelected() {}
        })
    }

    private fun mostrarGraficoTipoTarima(lista: List<MaterialItem>) {
        val coloresTarima = listOf(
            android.graphics.Color.parseColor("#FF6384"),
            android.graphics.Color.parseColor("#36A2EB"),
            android.graphics.Color.parseColor("#4CAF50"),
            android.graphics.Color.parseColor("#FFC107"),
            android.graphics.Color.parseColor("#8E24AA")
        )
        val conteoPorTipoTarima = lista.groupBy { it.tipoTarima }.mapValues { (_, items) ->
            items.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
        }
        val entries = conteoPorTipoTarima.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(entries, "Tipo de Tarima")
        dataSet.colors = coloresTarima
        dataSet.valueTextSize = 14f
        dataSet.valueFormatter = PercentFormatter(pieChartTipoTarima)
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 8f
        val data = PieData(dataSet)
        data.setValueTextSize(14f)
        data.setValueTextColor(android.graphics.Color.WHITE)
        pieChartTipoTarima.data = data
        pieChartTipoTarima.setUsePercentValues(true)
        pieChartTipoTarima.description.isEnabled = false
        pieChartTipoTarima.centerText = "Tipo de Tarima"
        pieChartTipoTarima.setEntryLabelColor(android.graphics.Color.BLACK)
        pieChartTipoTarima.legend.isEnabled = true
        pieChartTipoTarima.legend.textSize = 14f
        pieChartTipoTarima.legend.isWordWrapEnabled = true
        pieChartTipoTarima.animateY(1000)
        pieChartTipoTarima.invalidate()
    }

    private fun configurarGraficos() {
        // Configurar gráfico principal
        pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(android.graphics.Color.TRANSPARENT)
            setTransparentCircleColor(android.graphics.Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 30f
            transparentCircleRadius = 35f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
        }

        // Configurar gráfico de tipo de tarima
        pieChartTipoTarima.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(android.graphics.Color.TRANSPARENT)
            holeRadius = 25f
            transparentCircleRadius = 30f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
        }

        // Actualizar gráficos iniciales
        actualizarGraficos()
    }

    private fun actualizarGraficos() {
        val lista = getMaterialesFiltradosPorAlmacen()
        mostrarGraficoPorSKU(lista)
        mostrarGraficoTipoTarima(lista)
        actualizarEstadisticasAvanzadas(lista)
    }

    private fun actualizarEstadisticasAvanzadas(lista: List<MaterialItem>) {
        // Actualizar estadísticas de completitud
        val totalItems = lista.size
        val totalPallets = lista.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }

        // Calcular porcentaje de completitud (simulado)
        val porcentajeCompletitud = if (totalItems > 0) {
            minOf(100, (totalPallets * 100) / maxOf(1, totalItems * 5)) // Simulación
        } else 0

        tvPorcentajeCompletitud.text = "$porcentajeCompletitud%"

        // Actualizar última actualización
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        tvUltimaActualizacion.text = "🕒 Última actualización: ${dateFormat.format(Date())}"

        // Calcular discrepancias críticas (simulado)
        val discrepancias = lista.count { item ->
            val pallets = item.totalPallets?.toIntOrNull() ?: 0
            pallets == 0 || item.restos?.isNotEmpty() == true
        }

        tvDiscrepanciasCriticas.text = "⚠️ Discrepancias críticas: $discrepancias"

        // Actualizar alertas
        actualizarAlertas(lista, discrepancias)
    }

    private fun actualizarAlertas(lista: List<MaterialItem>, discrepancias: Int) {
        layoutAlertas.removeAllViews()

        if (discrepancias == 0 && lista.isNotEmpty()) {
            val alertaPositiva = TextView(this).apply {
                text = "✅ No hay alertas críticas - Sistema funcionando correctamente"
                setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                textSize = 14f
                setPadding(16, 8, 16, 8)
            }
            layoutAlertas.addView(alertaPositiva)
        } else {
            if (lista.isEmpty()) {
                val alertaVacia = TextView(this).apply {
                    text = "📭 No hay datos de inventario cargados"
                    setTextColor(android.graphics.Color.parseColor("#FF9800"))
                    textSize = 14f
                    setPadding(16, 8, 16, 8)
                }
                layoutAlertas.addView(alertaVacia)
            }

            if (discrepancias > 0) {
                val alertaDiscrepancias = TextView(this).apply {
                    text = "⚠️ Se detectaron $discrepancias items con discrepancias - Revisar urgente"
                    setTextColor(android.graphics.Color.parseColor("#F44336"))
                    textSize = 14f
                    setPadding(16, 8, 16, 8)
                }
                layoutAlertas.addView(alertaDiscrepancias)
            }
        }
    }

    // Si quieres enviar el resumen por correo, puedes usar esta función:
    private fun sendSummaryByEmail() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("inventario@kof.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Resumen de Inventario")
            putExtra(Intent.EXTRA_TEXT, buildString {
                appendLine("Resumen de Inventario")
                appendLine("Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}")
                appendLine()
                appendLine("Total Items: ${tvTotalItems.text}")
                appendLine("Total Pallets: ${tvTotalPallets.text}")
            })
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(intent, "Enviar resumen por correo"))
        } else {
            Toast.makeText(this, "No se encontró una aplicación de correo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportarResumen() {
        // Exportar solo los datos del almacén seleccionado
        val nombreAlmacen = almacenSeleccionado?.nombreAlmacen ?: "Todos"
        val lista = getMaterialesFiltradosPorAlmacen()
        
        // Crear una copia local del SKU seleccionado para evitar problemas de smart cast
        val skuLocal = skuSeleccionado
        
        // Filtrar por SKU si hay uno seleccionado
        val listaFiltrada = if (skuLocal.isNullOrEmpty() || skuLocal == "Todos") 
            lista 
        else 
            lista.filter { it.sku == skuLocal }
            
        if (listaFiltrada.isEmpty()) {
            Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show()
            return
        }
        
        val sb = StringBuilder()
        sb.appendLine("SKU,Descripción,Centro,Línea,OP,F. Producción,Días V,Ubicación,Pallets,Restos,Tipo Tarima,Almacén")
        
        // Usar comillas para evitar problemas con comas en los datos
        listaFiltrada.forEach { item ->
            sb.appendLine("\"${item.sku}\",\"${item.descripcion}\",\"${item.centro}\",\"${item.linea}\",\"${item.op}\",\"${item.fProd}\",\"${item.diasV}\",\"${item.ubicacion}\",\"${item.totalPallets}\",\"${item.restos}\",\"${item.tipoTarima}\",\"${item.almacen}\"")
        }
        
        val almacenPart = if (nombreAlmacen == "Todos") "todos_almacenes" else nombreAlmacen.replace(" ", "_")
        val skuPart = if (skuLocal.isNullOrEmpty() || skuLocal == "Todos") {
            "todos_skus"
        } else {
            skuLocal.replace("/", "_")
        }
        val fileName = "resumen_inventario_${almacenPart}_${skuPart}_${java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())}.csv"
        
        try {
            val file = File(getExternalFilesDir("Reportes"), fileName)
            file.parentFile?.mkdirs()
            file.writeText(sb.toString())
            Toast.makeText(this, "📄 Resumen exportado: ${file.name}", Toast.LENGTH_LONG).show()
            
            // Mostrar opción para compartir el archivo
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/csv"
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                file
            )
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "Compartir resumen de inventario"))
            
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarDetallesCompletos() {
        val lista = getMaterialesFiltradosPorAlmacen()
        val detallesCompletos = lista.joinToString("\n\n") {
            """
            SKU: ${it.sku}
            Descripción: ${it.descripcion}
            Centro: ${it.centro}
            Línea: ${it.linea}
            OP: ${it.op}
            F. Producción: ${it.fProd}
            Días V: ${it.diasV}
            Ubicación: ${it.ubicacion}
            Pallets: ${it.totalPallets}
            Restos: ${it.restos}
            Tipo Tarima: ${it.tipoTarima}
            """.trimIndent()
        }

        AlertDialog.Builder(this)
            .setTitle("📋 Detalles Completos del Inventario")
            .setMessage(if (detallesCompletos.isNotEmpty()) detallesCompletos else "📭 No hay datos disponibles")
            .setPositiveButton("Cerrar", null)
            .setNeutralButton("Exportar") { _, _ ->
                exportarResumen()
            }
            .show()
    }

    private fun mostrarTendencias() {
        val lista = getMaterialesFiltradosPorAlmacen()
        val alertas = mutableListOf<String>()

        // Análisis de tendencias
        val totalItems = lista.size
        val totalPallets = lista.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
        val promedioPS = if (totalItems > 0) totalPallets.toDouble() / totalItems else 0.0

        // SKUs más frecuentes
        val skusFrecuentes = lista.groupBy { it.sku }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        // Centros más activos
        val centrosActivos = lista.groupBy { it.centro }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(3)

        val mensaje = buildString {
            appendLine("📈 ANÁLISIS DE TENDENCIAS")
            appendLine("=" * 30)
            appendLine()
            appendLine("📊 ESTADÍSTICAS GENERALES:")
            appendLine("• Total de items: $totalItems")
            appendLine("• Total de pallets: $totalPallets")
            appendLine("• Promedio pallets/item: ${String.format("%.1f", promedioPS)}")
            appendLine()

            if (skusFrecuentes.isNotEmpty()) {
                appendLine("🔝 TOP 5 SKUs MÁS FRECUENTES:")
                skusFrecuentes.forEachIndexed { index, (sku, count) ->
                    appendLine("${index + 1}. $sku ($count items)")
                }
                appendLine()
            }

            if (centrosActivos.isNotEmpty()) {
                appendLine("🏢 CENTROS MÁS ACTIVOS:")
                centrosActivos.forEachIndexed { index, (centro, count) ->
                    appendLine("${index + 1}. $centro ($count items)")
                }
                appendLine()
            }

            // Alertas de gestión
            if (totalItems == 0) {
                appendLine("⚠️ ALERTA: No hay datos de inventario")
            } else if (totalItems < 10) {
                appendLine("⚠️ ALERTA: Bajo volumen de datos ($totalItems items)")
            } else {
                appendLine("✅ Volumen de datos normal")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("📈 Tendencias y Análisis")
            .setMessage(mensaje)
            .setPositiveButton("Cerrar", null)
            .setNeutralButton("Enviar por Email") { _, _ ->
                sendSummaryByEmail()
            }
            .show()
    }

    private fun mostrarDialogoLimpiarDatos() {
        AlertDialog.Builder(this)
            .setTitle("🗑️ Limpiar Datos de Inventario")
            .setMessage("¿Está seguro de que desea eliminar todos los datos del inventario?\n\n⚠️ Esta acción no se puede deshacer.")
            .setPositiveButton("SÍ, LIMPIAR") { _, _ ->
                viewModel.resetearInventario { success, msg ->
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        actualizarResumen()
                        actualizarSpinnerSKU()
                        actualizarGraficos()
                        Toast.makeText(this, "✅ Inventario limpiado correctamente", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun mostrarDialogoSeleccionAlmacen() {
        val almacenes = almacenManager.obtenerAlmacenes()
        if (almacenes.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Sin almacenes configurados")
                .setMessage("Debes configurar al menos un almacén para ver el resumen.")
                .setPositiveButton("OK") { _, _ -> finish() }
                .setCancelable(false)
                .show()
            return
        }
        
        // Crear una lista con la opción "Todos" al principio
        val todosOpcion = AlmacenCapacidad("Todos", 0)
        val almacenesConTodos = listOf(todosOpcion) + almacenes
        val nombres = almacenesConTodos.map { it.nombreAlmacen }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("Selecciona un almacén")
            .setItems(nombres) { _, which ->
                almacenSeleccionado = almacenesConTodos[which]
                Toast.makeText(this, "Almacén seleccionado: ${almacenesConTodos[which].nombreAlmacen}", Toast.LENGTH_SHORT).show()
                // Cargar y mostrar los datos filtrados por almacén
                cargarResumenPorAlmacen()
            }
            .setCancelable(false)
            .show()
    }

    private fun cargarResumenPorAlmacen() {
        // Obtener los materiales filtrados por el almacén seleccionado
        val lista = getMaterialesFiltradosPorAlmacen()
        listaMateriales = lista
        // Actualizar UI con los datos filtrados
        actualizarResumenPorAlmacen(lista)
        // Actualizar el spinner de SKUs con los datos filtrados
        actualizarSpinnerSKU()
    }

    private fun actualizarResumenPorAlmacen(lista: List<MaterialItem>) {
        // Actualiza los totales, lista y gráficas solo con los datos del almacén seleccionado
        tvTotalItems.text = lista.size.toString()
        val totalPallets = lista.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
        tvTotalPallets.text = totalPallets.toString()

        val detalles = if (mostrarDetalles) {
            lista.takeLast(10).map {
                """
                SKU: ${it.sku}
                Descripción: ${it.descripcion}
                Centro: ${it.centro}
                Línea: ${it.linea}
                OP: ${it.op}
                F. Producción: ${it.fProd}
                Días V: ${it.diasV}
                Ubicación: ${it.ubicacion}
                Pallets: ${it.totalPallets}
                Restos: ${it.restos}
                Tipo Tarima: ${it.tipoTarima}
                """.trimIndent()
            }
        } else {
            lista.takeLast(10).map {
                "SKU: ${it.sku}\nDesc: ${it.descripcion}\nPallets: ${it.totalPallets ?: "-"}"
            }
        }
        lvRecentItems.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, detalles)
        mostrarGraficoPorSKU(lista)
        mostrarGraficoTipoTarima(lista)
        // ...puedes actualizar más elementos de la UI aquí si es necesario...
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Puedes agregar aquí la lógica para manejar los ítems del menú de navegación
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}

// Extensión para repetir strings (usado en reportes)
private operator fun String.times(n: Int): String = this.repeat(n)
