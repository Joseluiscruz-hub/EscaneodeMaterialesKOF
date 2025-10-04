package com.example.escaneodematerialeskof

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.escaneodematerialeskof.ui.config.ConfiguracionActivity
import com.example.escaneodematerialeskof.ui.opciones.MasOpcionesActivity
import com.example.escaneodematerialeskof.viewmodel.PerplexityViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var barChart: BarChart
    private lateinit var perplexityViewModel: PerplexityViewModel
    private lateinit var capturaViewModel: CapturaInventarioViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_dashboard_animated)

        // Inicializar el ViewModel externo
        perplexityViewModel = ViewModelProvider(this).get(PerplexityViewModel::class.java)
        // ViewModel con datos de inventario real
        capturaViewModel = ViewModelProvider(this).get(CapturaInventarioViewModel::class.java)

        // Inicializar las vistas y componentes de la UI
        initializeViews()
        setupNavigationDrawer()
        setupBarChart(emptyList(), emptyList())
        setupClickListeners()
        observeViewModel()

        // Pull-to-refresh para recargar KPIs/Gráfica
        val swipe = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipe_refresh)
        swipe.setOnRefreshListener { refreshDashboard() }
    }

    override fun onResume() {
        super.onResume()
        refreshDashboard()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        barChart = findViewById(R.id.bar_chart)
    }

    private fun setupNavigationDrawer() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_inicio -> drawerLayout.closeDrawer(GravityCompat.START)
                R.id.nav_capturar_inventario -> startActivity(Intent(this, CapturaInventarioActivity::class.java))
                R.id.nav_dashboard, R.id.nav_resumen_inventario -> startActivity(
                    Intent(
                        this,
                        NewInventarioResumenActivity::class.java
                    )
                )

                R.id.nav_comparar_inventarios -> startActivity(Intent(this, InventoryComparisonActivity::class.java))
                R.id.nav_ajuste_inventario -> Toast.makeText(
                    this,
                    "Función de Ajuste no implementada",
                    Toast.LENGTH_SHORT
                ).show()

                R.id.nav_configuracion -> startActivity(Intent(this, ConfiguracionActivity::class.java))
                R.id.nav_mas_opciones -> startActivity(Intent(this, MasOpcionesActivity::class.java))
                R.id.nav_restos -> startActivity(Intent(this, RestosActivity::class.java))
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupBarChart(entries: List<BarEntry>, labels: List<String>) {
        val dataSet = BarDataSet(entries, "Pallets por tipo de tarima").apply {
            colors = listOf(
                Color.rgb(103, 58, 183), // Deep Purple
                Color.rgb(33, 150, 243),  // Blue
                Color.rgb(0, 150, 136),   // Teal
                Color.rgb(255, 193, 7)    // Amber
            )
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        barChart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                setDrawGridLines(false)
                granularity = 1f
                labelRotationAngle = -15f
            }
            axisRight.isEnabled = false
            axisLeft.apply {
                setDrawGridLines(false)
                axisMinimum = 0f
            }
            animateY(800)
            invalidate()
        }
    }

    private fun setupClickListeners() {
        findViewById<MaterialButton>(R.id.btn_scan).setOnClickListener {
            startActivity(Intent(this, CapturaInventarioActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btn_view_inventory).setOnClickListener {
            startActivity(Intent(this, NewInventarioResumenActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btn_test_perplexity).setOnClickListener {
            val token = BuildConfig.PERPLEXITY_API_KEY
            if (token.isBlank()) {
                Toast.makeText(this, "⚠️ Configura tu API key en apikeys.properties", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val question = "¿Cuáles son las mejores prácticas para organizar un almacén de bebidas?"
            perplexityViewModel.getAnswer(token, question)
            Toast.makeText(this, "Consultando a Perplexity AI...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        perplexityViewModel.answer.observe(this) { response ->
            val answer = response.choices.firstOrNull()?.message?.content ?: "Sin respuesta"
            Toast.makeText(this, "Respuesta: $answer", Toast.LENGTH_LONG).show()
        }

        perplexityViewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshDashboard() {
        val swipe = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipe_refresh)
        swipe.isRefreshing = true

        val tvScanned = findViewById<TextView>(R.id.tv_kpi_scanned)
        val tvSync = findViewById<TextView>(R.id.tv_kpi_sync)

        lifecycleScope.launch(Dispatchers.IO) {
            // Obtener datos de inventario
            val totalPallets = capturaViewModel.obtenerTotalPallets()
            val resumenPorTipo = capturaViewModel.obtenerResumenPorTipoTarima() // Map<Tipo, Total>
            val discrepancias = capturaViewModel.obtenerDiscrepanciasCriticas()

            // Preparar datos para la gráfica
            val labels = resumenPorTipo.keys.toList()
            val entries = labels.mapIndexed { index, tipo ->
                BarEntry(index.toFloat(), (resumenPorTipo[tipo] ?: 0).toFloat())
            }

            withContext(Dispatchers.Main) {
                // KPIs
                tvScanned.text = totalPallets.toString()
                tvSync.text = if (discrepancias == 0) "Sin discrepancias" else "$discrepancias discrepancias"

                // Gráfica
                setupBarChart(entries, labels)
                swipe.isRefreshing = false
            }
        }
    }
}