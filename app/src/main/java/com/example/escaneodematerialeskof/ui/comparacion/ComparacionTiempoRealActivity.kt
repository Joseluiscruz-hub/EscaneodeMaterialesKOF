package com.example.escaneodematerialeskof.ui.comparacion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.escaneodematerialeskof.R
import com.example.escaneodematerialeskof.dashboard.ComparacionInventario
import com.example.escaneodematerialeskof.databinding.ActivityComparacionTiempoRealBinding
import com.example.escaneodematerialeskof.viewmodel.InventoryComparisonViewModel
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.io.File

/**
 * Actividad para mostrar comparación de inventario en tiempo real mientras se escanea.
 */
class ComparacionTiempoRealActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityComparacionTiempoRealBinding
    private val viewModel: InventoryComparisonViewModel by viewModels()
    private lateinit var comparacionAdapter: ComparacionAdapter
    private lateinit var toggle: ActionBarDrawerToggle

    // Launcher para importar inventario del sistema
    private val importSistemaLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                try {
                    val success = viewModel.importarInventarioSistema(it)
                    if (success) {
                        Toast.makeText(
                            this@ComparacionTiempoRealActivity,
                            "Inventario del sistema cargado correctamente. La comparación se actualizará automáticamente.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ComparacionTiempoRealActivity,
                            "Error al cargar el inventario del sistema. Revisa el formato del archivo.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@ComparacionTiempoRealActivity,
                        "Error al procesar el archivo: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComparacionTiempoRealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupDrawer()

        // Manejo de back con OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // Mostrar instrucciones iniciales
        mostrarInstrucciones()
    }

    private fun setupRecyclerView() {
        comparacionAdapter = ComparacionAdapter()
        binding.recyclerViewComparacion.apply {
            layoutManager = LinearLayoutManager(this@ComparacionTiempoRealActivity)
            adapter = comparacionAdapter
        }
    }

    private fun setupObservers() {
        // Observar cambios en la comparación detallada (tiempo real)
        viewModel.comparacionDetallada.observe(this) { comparacion ->
            comparacionAdapter.submitList(comparacion)
            actualizarEstadisticas(comparacion)

            // Mostrar/ocultar contenido según si hay datos
            if (comparacion.isEmpty()) {
                binding.recyclerViewComparacion.visibility = View.GONE
                binding.layoutEstadisticas.visibility = View.GONE
                binding.textInstrucciones.visibility = View.VISIBLE
            } else {
                binding.recyclerViewComparacion.visibility = View.VISIBLE
                binding.layoutEstadisticas.visibility = View.VISIBLE
                binding.textInstrucciones.visibility = View.GONE
            }
        }

        // Observar mensajes del ViewModel
        viewModel.mensaje.observe(this) { mensaje ->
            mensaje?.let {
                binding.textEstado.text = it
            }
        }

        // Observar errores del ViewModel
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
                binding.textEstado.text = "Error: $it"
            }
        }

        // Observar estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar cambios en el inventario del sistema
        viewModel.inventarioSistema.observe(this) { inventario ->
            binding.textInventarioSistema.text = "📊 Sistema: ${inventario.size} items"
        }

        // Observar cambios en el inventario escaneado para mostrar progreso
        viewModel.inventarioEscaneado.observe(this) { inventario ->
            val totalEscaneado = inventario.values.sumOf { it.totalPallets }
            binding.textItemsEscaneados.text = "📱 Escaneados: ${inventario.size} (${totalEscaneado} pallets)"
        }
    }

    private fun setupClickListeners() {
        // Los click listeners ahora se manejan desde el Navigation Drawer
        // No hay botones en la pantalla principal
    }

    private fun setupDrawer() {
        // Configurar el Toolbar
        setSupportActionBar(binding.toolbar)

        // Configurar el Navigation Drawer
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navigationView: NavigationView = binding.navigationView

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun mostrarDialogoSeleccionArchivo() {
        AlertDialog.Builder(this)
            .setTitle("Cargar Inventario del Sistema")
            .setMessage("Seleccione un archivo CSV con el inventario de referencia del sistema.")
            .setPositiveButton("Seleccionar Archivo") { _, _ ->
                importSistemaLauncher.launch("text/*")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun toggleTiempoReal() {
        // Simplificar la lógica sin acceder a propiedades privadas
        viewModel.habilitarActualizacionTiempoReal(false)
        binding.textEstado.text = "Función de pausa disponible en el menú lateral"
        Toast.makeText(this, "Use el menú lateral para controlar el tiempo real", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarDialogoLimpiarDatos() {
        AlertDialog.Builder(this)
            .setTitle("Limpiar Datos")
            .setMessage("¿Qué datos desea limpiar?")
            .setPositiveButton("Solo Escaneados") { _, _ ->
                viewModel.clearScannedInventory()
                Toast.makeText(this, "Inventario escaneado limpiado", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Solo Comparación") { _, _ ->
                viewModel.clearComparisonData()
                Toast.makeText(this, "Datos de comparación limpiados", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Todo") { _, _ ->
                viewModel.limpiarDatos()
                Toast.makeText(this, "Todos los datos limpiados", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun actualizarEstadisticas(comparacion: List<ComparacionInventario>) {
        val coincidencias = comparacion.count { it.estado == "OK" }
        val faltantes = comparacion.count { it.estado.contains("Faltante") }
        val sobrantes = comparacion.count { it.estado.contains("Sobrante") }

        binding.textCoincidencias.text = "✅ Coincidencias: $coincidencias"
        binding.textFaltantes.text = "❌ Faltantes: $faltantes"
        binding.textSobrantes.text = "⚠️ Sobrantes: $sobrantes"

        // Calcular porcentaje de precisión
        val total = comparacion.size
        val precision = if (total > 0) (coincidencias * 100 / total) else 0
        binding.textPrecision.text = "📊 Precisión: $precision%"

        // Actualizar indicador visual de progreso
        binding.progressPrecision.progress = precision

        // Cambiar color del progreso según la precisión
        val color = when {
            precision >= 90 -> android.graphics.Color.GREEN
            precision >= 70 -> android.graphics.Color.YELLOW
            else -> android.graphics.Color.RED
        }
        binding.progressPrecision.progressTintList = android.content.res.ColorStateList.valueOf(color)
    }

    private fun mostrarInstrucciones() {
        binding.textInstrucciones.text = """
            📋 Instrucciones para Comparación en Tiempo Real:
            
            1️⃣ Cargue el inventario del sistema (archivo CSV)
            2️⃣ Presione "Iniciar Escaneo" para comenzar
            3️⃣ Los datos se actualizarán automáticamente mientras escanea
            4️⃣ Puede pausar/reanudar la actualización en cualquier momento
            5️⃣ Exporte los resultados cuando termine
            
            ⚡ La comparación se actualiza automáticamente con cada escaneo
            🔄 Use "Actualizar Manual" si necesita forzar una actualización
        """.trimIndent()
    }

    private fun exportarComparacion() {
        val comparacion = viewModel.comparacionDetallada.value
        if (comparacion.isNullOrEmpty()) {
            Toast.makeText(this, "No hay datos de comparación para exportar", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Crear un archivo CSV para exportar la comparación
                val timeStamp = System.currentTimeMillis()
                val fileName = "comparacion_tiempo_real_$timeStamp.csv"
                val directory = getExternalFilesDir("Comparaciones")
                if (directory?.exists() != true) {
                    directory?.mkdirs()
                }
                val file = File(directory, fileName)

                // Escribir datos al CSV
                file.writer().use { writer ->
                    // Encabezado
                    writer.write("SKU,Descripción,Sistema,Escaneado,Diferencia,Estado,Tipo Tarima\n")

                    // Datos de comparación
                    comparacion.forEach { item ->
                        writer.write("${item.sku},${item.descripcion},${item.inventario ?: 0},${item.escaneado ?: 0},${item.diferencia ?: 0},${item.estado},${item.tipoTarima}\n")
                    }
                }

                runOnUiThread {
                    Toast.makeText(
                        this@ComparacionTiempoRealActivity,
                        "Comparación exportada: ${file.name}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Compartir el archivo
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/csv"
                    val fileUri = FileProvider.getUriForFile(
                        this@ComparacionTiempoRealActivity,
                        "${packageName}.provider",
                        file
                    )
                    intent.putExtra(Intent.EXTRA_STREAM, fileUri)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(Intent.createChooser(intent, "Compartir comparación"))
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@ComparacionTiempoRealActivity,
                        "Error al exportar: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Asegurar que la actualización en tiempo real esté activa al regresar
        viewModel.habilitarActualizacionTiempoReal(true)
    }

    private fun showTipoEscaneoDialog() {
        val tipos = arrayOf("Rumba", "Pallet", "Manual")
        AlertDialog.Builder(this)
            .setTitle("Seleccionar tipo de escaneo")
            .setItems(tipos) { dialog, which ->
                val tipo = when (which) {
                    0 -> "rumba"
                    1 -> "pallet"
                    2 -> "manual"
                    else -> ""
                }
                if (tipo.isNotEmpty()) {
                    val intent = Intent(this, com.example.escaneodematerialeskof.CapturaInventarioActivity::class.java)
                    intent.putExtra(
                        com.example.escaneodematerialeskof.CapturaInventarioActivity.EXTRA_TIPO_ESCANEO,
                        tipo
                    )
                    startActivity(intent)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Manejar clic en elementos del Navigation Drawer
        when (item.itemId) {
            R.id.nav_cargar_sistema -> {
                mostrarDialogoSeleccionArchivo()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }

            R.id.nav_iniciar_escaneo -> {
                if (viewModel.inventarioSistema.value?.isEmpty() != false) {
                    Toast.makeText(this, "Primero debe cargar el inventario del sistema", Toast.LENGTH_SHORT).show()
                } else {
                    showTipoEscaneoDialog()
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }

            R.id.nav_pausar_tiempo_real -> {
                toggleTiempoReal()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }

            R.id.nav_actualizar_manual -> {
                viewModel.actualizarComparacion()
                Toast.makeText(this, "Actualizando comparación...", Toast.LENGTH_SHORT).show()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }

            R.id.nav_exportar_comparacion -> {
                exportarComparacion()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }

            R.id.nav_limpiar_datos -> {
                mostrarDialogoLimpiarDatos()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }

            R.id.nav_instrucciones -> {
                mostrarInstruccionesDetalladas()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }

            R.id.nav_configuracion -> {
                Toast.makeText(this, "Configuración - Próximamente", Toast.LENGTH_SHORT).show()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        return true
    }

    private fun mostrarInstruccionesDetalladas() {
        AlertDialog.Builder(this)
            .setTitle("📋 Instrucciones Detalladas")
            .setMessage(
                """
                ⚡ COMPARACIÓN EN TIEMPO REAL
                
                1️⃣ PREPARACIÓN:
                • Cargue el inventario del sistema desde archivo CSV
                • Verifique que los datos se hayan importado correctamente
                
                2️⃣ INICIO DE ESCANEO:
                • Presione "Iniciar Escaneo" en el menú lateral
                • Seleccione el tipo de escaneo (Rumba/Pallet/Manual)
                • Comience a escanear los códigos QR
                
                3️⃣ MONITOREO:
                • La comparación se actualiza automáticamente
                • Observe las estadísticas en tiempo real
                • Use "Pausar" si necesita detener temporalmente
                
                4️⃣ GESTIÓN:
                • "Actualizar Manual" para forzar actualización
                • "Limpiar Datos" para resetear información
                • "Exportar" para guardar resultados
                
                💡 CONSEJOS:
                • Mantenga la app abierta durante el escaneo
                • Revise las estadísticas periódicamente
                • Exporte los datos antes de limpiar
            """.trimIndent()
            )
            .setPositiveButton("Entendido", null)
            .show()
    }
}
