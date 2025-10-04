package com.example.escaneodematerialeskof.ui.resumen

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.escaneodematerialeskof.R
import com.example.escaneodematerialeskof.model.MaterialItem
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class InventarioResumenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario_resumen_material3)

        // Configurar Toolbar
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(topAppBar)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        // Referencias a las vistas
        val btnRefresh = findViewById<MaterialButton>(R.id.btnRefresh)
        val btnExportSummary = findViewById<MaterialButton>(R.id.btnExportSummary)
        val btnViewDetails = findViewById<MaterialButton>(R.id.btnViewDetails)
        val btnClearData = findViewById<MaterialButton>(R.id.btnClearData)
        val btnComparacionRapida = findViewById<MaterialButton>(R.id.btnComparacionRapida)
        val btnImportInventory = findViewById<MaterialButton>(R.id.btnImportInventory)
        val btnVerTendencias = findViewById<MaterialButton>(R.id.btnVerTendencias)

        val tvTotalItems = findViewById<TextView>(R.id.tvTotalItems)
        val tvTotalPallets = findViewById<TextView>(R.id.tvTotalPallets)
        val tvPorcentajeCompletitud = findViewById<TextView>(R.id.tvPorcentajeCompletitud)
        val tvUltimaActualizacion = findViewById<TextView>(R.id.tvUltimaActualizacion)
        val tvDiscrepanciasCriticas = findViewById<TextView>(R.id.tvDiscrepanciasCriticas)

        val lvRecentItems = findViewById<ListView>(R.id.lvRecentItems)
        val autoCompleteSKU = findViewById<AutoCompleteTextView>(R.id.spinnerCentro)
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val pieChartTipoTarima = findViewById<PieChart>(R.id.pieChartTipoTarima)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        val gson = Gson()
        val prefs = getSharedPreferences("inventario_escaneos", MODE_PRIVATE)
        val type = object : TypeToken<List<MaterialItem>>() {}.type

        fun cargarDatos(): List<MaterialItem> {
            val listaJson = prefs.getString("lista_materiales", null)
            return if (listaJson != null) gson.fromJson(listaJson, type) else emptyList()
        }

        fun actualizarAutoCompleteSKU(lista: List<MaterialItem>) {
            val skus = mutableListOf("Todos") + lista.map { it.sku }.distinct().sorted()
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, skus)
            autoCompleteSKU.setAdapter(adapter)
            if (autoCompleteSKU.text.isEmpty()) {
                autoCompleteSKU.setText("Todos", false)
            }
        }

        fun mostrarGraficoPorSKU(lista: List<MaterialItem>, skuSeleccionado: String?) {
            val datosFiltrados = if (skuSeleccionado.isNullOrEmpty() || skuSeleccionado == "Todos")
                lista else lista.filter { it.sku == skuSeleccionado }

            val palletsPorSKU = datosFiltrados.groupBy { it.sku }.mapValues { (_, items) ->
                items.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
            }

            val entries = palletsPorSKU.map { PieEntry(it.value.toFloat(), it.key) }
            val dataSet = PieDataSet(entries, "Pallets por SKU")

            dataSet.colors = listOf(
                android.graphics.Color.parseColor("#1976D2"),
                android.graphics.Color.parseColor("#E53935"),
                android.graphics.Color.parseColor("#43A047"),
                android.graphics.Color.parseColor("#FBC02D"),
                android.graphics.Color.parseColor("#8E24AA"),
                android.graphics.Color.parseColor("#00838F"),
                android.graphics.Color.parseColor("#F57C00")
            )
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = android.graphics.Color.WHITE
            dataSet.sliceSpace = 2f

            pieChart.setUsePercentValues(true)
            pieChart.description.isEnabled = false
            pieChart.legend.isEnabled = true
            pieChart.legend.textColor = resources.getColor(R.color.colorOnSurface, theme)
            pieChart.setEntryLabelColor(android.graphics.Color.BLACK)

            val data = PieData(dataSet)
            pieChart.data = data
            pieChart.invalidate()
        }

        fun mostrarGraficoPorTipoTarima(lista: List<MaterialItem>, skuSeleccionado: String?) {
            val datosFiltrados = if (skuSeleccionado.isNullOrEmpty() || skuSeleccionado == "Todos")
                lista else lista.filter { it.sku == skuSeleccionado }

            val palletsPorTipo = datosFiltrados.groupBy { it.tipoTarima ?: "Sin definir" }
                .mapValues { (_, items) -> items.sumOf { it.totalPallets?.toIntOrNull() ?: 0 } }

            val entries = palletsPorTipo.map { PieEntry(it.value.toFloat(), it.key) }
            val dataSet = PieDataSet(entries, "Por Tipo")

            dataSet.colors = listOf(
                android.graphics.Color.parseColor("#4CAF50"),
                android.graphics.Color.parseColor("#FF9800"),
                android.graphics.Color.parseColor("#2196F3"),
                android.graphics.Color.parseColor("#9C27B0")
            )
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = android.graphics.Color.WHITE
            dataSet.sliceSpace = 2f

            pieChartTipoTarima.setUsePercentValues(true)
            pieChartTipoTarima.description.isEnabled = false
            pieChartTipoTarima.legend.isEnabled = true
            pieChartTipoTarima.legend.textColor = resources.getColor(R.color.colorOnSurface, theme)
            pieChartTipoTarima.setEntryLabelColor(android.graphics.Color.BLACK)

            val data = PieData(dataSet)
            pieChartTipoTarima.data = data
            pieChartTipoTarima.invalidate()
        }

        fun actualizarResumen(skuSeleccionado: String? = null) {
            val lista = cargarDatos()
            val datosFiltrados = if (skuSeleccionado.isNullOrEmpty() || skuSeleccionado == "Todos")
                lista else lista.filter { it.sku == skuSeleccionado }

            tvTotalItems.text = datosFiltrados.size.toString()
            val totalPallets = datosFiltrados.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
            tvTotalPallets.text = totalPallets.toString()

            // Calcular completitud (ejemplo: basado en campos llenos)
            val completitud = if (datosFiltrados.isEmpty()) 0 else {
                val camposCompletos = datosFiltrados.count {
                    !it.sku.isNullOrEmpty() && !it.descripcion.isNullOrEmpty() &&
                            !it.totalPallets.isNullOrEmpty() && !it.ubicacion.isNullOrEmpty()
                }
                (camposCompletos * 100) / datosFiltrados.size
            }
            tvPorcentajeCompletitud.text = "$completitud%"

            // Última actualización
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvUltimaActualizacion.text = "Última actualización: ${sdf.format(Date())}"

            // Discrepancias (ejemplo simple)
            tvDiscrepanciasCriticas.text = "Discrepancias críticas: 0"

            val detalles = if (datosFiltrados.isEmpty())
                listOf("No hay datos escaneados para mostrar.")
            else
                datosFiltrados.takeLast(10).map {
                    "SKU: ${it.sku}\nDesc: ${it.descripcion}\nPallets: ${it.totalPallets ?: "-"}"
                }
            lvRecentItems.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, detalles)

            mostrarGraficoPorSKU(lista, skuSeleccionado)
            mostrarGraficoPorTipoTarima(lista, skuSeleccionado)
        }

        // Inicializar
        val listaInicial = cargarDatos()
        actualizarAutoCompleteSKU(listaInicial)
        actualizarResumen()

        // Listener para cambio de SKU
        autoCompleteSKU.setOnItemClickListener { _, _, _, _ ->
            val skuSeleccionado = autoCompleteSKU.text.toString()
            actualizarResumen(skuSeleccionado)
        }

        // Botones de acción
        btnRefresh.setOnClickListener {
            val lista = cargarDatos()
            actualizarAutoCompleteSKU(lista)
            actualizarResumen(autoCompleteSKU.text.toString())
            Toast.makeText(this, "✓ Datos actualizados", Toast.LENGTH_SHORT).show()
        }

        btnViewDetails.setOnClickListener {
            val lista = cargarDatos()
            val detalles = lista.map {
                "SKU: ${it.sku}\nDesc: ${it.descripcion}\nPallets: ${it.totalPallets ?: "-"}\nUbicación: ${it.ubicacion ?: "-"}"
            }.joinToString("\n\n")
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Detalles de Escaneos")
                .setMessage(if (detalles.isNotBlank()) detalles else "No hay escaneos guardados.")
                .setPositiveButton("OK", null)
                .show()
        }

        btnExportSummary.setOnClickListener {
            val lista = cargarDatos()
            val skuSeleccionado = autoCompleteSKU.text.toString()
            val datosFiltrados = if (skuSeleccionado.isEmpty() || skuSeleccionado == "Todos")
                lista else lista.filter { it.sku == skuSeleccionado }

            if (datosFiltrados.isEmpty()) {
                Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val csv = StringBuilder()
            csv.append("SKU,Descripción,CxPal,FPC,Con,Centro,LINEA,OP,FProd,DiasV,Ubicación,TotalPallets,Restos,TipoTarima\n")
            datosFiltrados.forEach {
                csv.append("\"${it.sku}\",\"${it.descripcion}\",\"${it.cxPal}\",\"${it.fpc}\",\"${it.con}\",\"${it.centro}\",\"${it.linea}\",\"${it.op}\",\"${it.fProd}\",\"${it.diasV}\",\"${it.ubicacion}\",\"${it.totalPallets}\",\"${it.restos}\",\"${it.tipoTarima}\"\n")
            }

            try {
                val uniqueId = UUID.randomUUID().toString().take(8)
                val baseName = if (skuSeleccionado.isEmpty() || skuSeleccionado == "Todos")
                    "resumen_inventario" else "resumen_inventario_${skuSeleccionado.replace("/", "_")}"
                val fileName = "${baseName}_${System.currentTimeMillis()}_${uniqueId}.csv"
                val file = java.io.File(filesDir, fileName)
                file.writeText(csv.toString())

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    "$packageName.fileprovider",
                    file
                )
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    setType("text/csv")
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Resumen de Inventario")
                    putExtra(android.content.Intent.EXTRA_TEXT, "Adjunto el resumen de inventario.")
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(android.content.Intent.createChooser(intent, "Enviar resumen por correo"))
            } catch (e: Exception) {
                Toast.makeText(this, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        btnClearData.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚠️ Confirmar")
                .setMessage("¿Está seguro de que desea limpiar todos los datos? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, limpiar") { _, _ ->
                    prefs.edit().remove("lista_materiales").apply()
                    actualizarAutoCompleteSKU(emptyList())
                    actualizarResumen()
                    Toast.makeText(this, "Datos limpiados correctamente", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btnComparacionRapida.setOnClickListener {
            Toast.makeText(this, "Función de comparación en desarrollo", Toast.LENGTH_SHORT).show()
        }

        btnImportInventory.setOnClickListener {
            Toast.makeText(this, "Función de importación en desarrollo", Toast.LENGTH_SHORT).show()
        }

        btnVerTendencias.setOnClickListener {
            Toast.makeText(this, "Función de tendencias en desarrollo", Toast.LENGTH_SHORT).show()
        }

        fabAdd.setOnClickListener {
            Toast.makeText(this, "Agregar nuevo item", Toast.LENGTH_SHORT).show()
        }

        // Bottom Navigation
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    finish()
                    true
                }

                R.id.nav_summary -> true
                else -> {
                    Toast.makeText(this, "Navegando a ${item.title}", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        }
        bottomNavigation.selectedItemId = R.id.nav_summary
    }
}
