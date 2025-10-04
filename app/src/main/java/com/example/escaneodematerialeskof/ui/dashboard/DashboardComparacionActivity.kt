package com.example.escaneodematerialeskof.dashboard

import android.graphics.Color
import android.os.Bundle
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.escaneodematerialeskof.databinding.ActivityDashboardComparacionBinding
import com.example.escaneodematerialeskof.manager.AlmacenCapacidadManager
import com.example.escaneodematerialeskof.model.AlmacenCapacidad
import com.example.escaneodematerialeskof.ui.dashboard.AlmacenCapacidadAdapter
import com.example.escaneodematerialeskof.CapturaInventarioViewModel
import androidx.activity.viewModels
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class DashboardComparacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardComparacionBinding
    private lateinit var comparacion: List<ComparacionInventario>
    private lateinit var almacenManager: AlmacenCapacidadManager
    private lateinit var almacenAdapter: AlmacenCapacidadAdapter
    private val capturaViewModel: CapturaInventarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardComparacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar manager y adapter para almacenes
        almacenManager = AlmacenCapacidadManager(this)
        setupAlmacenRecyclerView()

        @Suppress("UNCHECKED_CAST")
        comparacion = intent.getSerializableExtra("comparacion") as? List<ComparacionInventario> ?: emptyList()

        setupClickListeners()
        setupViews()

        // Cargar datos iniciales de capacidad
        actualizarAlmacenes()
        actualizarResumenSaturacion()

        // **VERIFICACIÓN DE DEBUG** - Mostrar toast para confirmar que la funcionalidad está activa
        Toast.makeText(this, "🏭 Gestión de capacidad de almacén ACTIVA", Toast.LENGTH_LONG).show()
    }

    private fun setupAlmacenRecyclerView() {
        almacenAdapter = AlmacenCapacidadAdapter(
            onEditarClick = { almacen -> mostrarDialogoEditarAlmacen(almacen) },
            onEliminarClick = { almacen -> mostrarDialogoEliminarAlmacen(almacen) }
        )

        try {
            binding.recyclerAlmacenes.apply {
                layoutManager = LinearLayoutManager(this@DashboardComparacionActivity)
                adapter = almacenAdapter
            }
        } catch (e: Exception) {
            // Si hay error con el binding, mostrar mensaje
            Toast.makeText(this, "Error al configurar RecyclerView: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        try {
            // Configurar almacén
            binding.btnConfigurarAlmacen.setOnClickListener {
                configurarNuevoAlmacen()
            }
        } catch (e: Exception) {
            // Si el botón no existe en el binding, crear uno temporal para debugging
            Toast.makeText(this, "⚠️ Botón configurar almacén no encontrado en el layout. Verificar binding.", Toast.LENGTH_LONG).show()
        }

        // Alternar entre gráfica y tabla (funcionalidad existente)
        binding.btnAlternarVista.setOnClickListener {
            if (binding.pieChart.visibility == android.view.View.VISIBLE) {
                binding.pieChart.visibility = android.view.View.GONE
                binding.tablaComparacion.visibility = android.view.View.VISIBLE
                binding.btnAlternarVista.text = getString(android.R.string.ok) // Usar string resource
            } else {
                binding.pieChart.visibility = android.view.View.VISIBLE
                binding.tablaComparacion.visibility = android.view.View.GONE
                binding.btnAlternarVista.text = getString(android.R.string.cancel) // Usar string resource
            }
        }

        // Exportar (funcionalidad existente)
        val nombreArchivo = "materiales_guardados_${System.currentTimeMillis()}.csv"
        binding.btnExportar.setOnClickListener {
            ComparadorInventario.exportarComparacionCSV(this, comparacion, nombreArchivo) { archivo ->
                archivo?.let { ComparadorInventario.enviarPorCorreo(this, it) }
            }
        }
    }

    private fun configurarNuevoAlmacen() {
        try {
            val nombreAlmacen = binding.etNombreAlmacen.text.toString().trim()
            val capacidadTexto = binding.etCapacidadMaxima.text.toString().trim()

            if (nombreAlmacen.isEmpty()) {
                Toast.makeText(this, "Ingrese el nombre del almacén", Toast.LENGTH_SHORT).show()
                return
            }

            if (capacidadTexto.isEmpty()) {
                Toast.makeText(this, "Ingrese la capacidad máxima", Toast.LENGTH_SHORT).show()
                return
            }

            val capacidadMaxima = capacidadTexto.toIntOrNull()
            if (capacidadMaxima == null || capacidadMaxima <= 0) {
                Toast.makeText(this, "Ingrese una capacidad válida", Toast.LENGTH_SHORT).show()
                return
            }

            // Obtener total de pallets escaneados actual
            val totalPalletsEscaneados = capturaViewModel.obtenerTotalPallets()

            val nuevoAlmacen = AlmacenCapacidad(
                nombreAlmacen = nombreAlmacen,
                capacidadMaxima = capacidadMaxima,
                palletsEscaneados = totalPalletsEscaneados
            )

            almacenManager.agregarOActualizarAlmacen(nuevoAlmacen)
            almacenManager.establecerAlmacenActual(nombreAlmacen)

            // Limpiar campos
            binding.etNombreAlmacen.text?.clear()
            binding.etCapacidadMaxima.text?.clear()

            // Actualizar UI
            actualizarAlmacenes()
            actualizarResumenSaturacion()

            Toast.makeText(this, "Almacén '$nombreAlmacen' configurado correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al configurar almacén: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarDialogoEditarAlmacen(almacen: AlmacenCapacidad) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)

        // Crear campos de entrada personalizados
        val etNombre = android.widget.EditText(this).apply {
            setText(almacen.nombreAlmacen)
            hint = "Nombre del almacén"
        }

        val etCapacidad = android.widget.EditText(this).apply {
            setText(almacen.capacidadMaxima.toString())
            hint = "Capacidad máxima"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            addView(etNombre)
            addView(etCapacidad)
        }

        builder.setTitle("Editar Almacén")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = etNombre.text.toString().trim()
                val nuevaCapacidad = etCapacidad.text.toString().toIntOrNull()

                if (nuevoNombre.isNotEmpty() && nuevaCapacidad != null && nuevaCapacidad > 0) {
                    // Si cambió el nombre, eliminar el anterior
                    if (nuevoNombre != almacen.nombreAlmacen) {
                        almacenManager.eliminarAlmacen(almacen.nombreAlmacen)
                    }

                    val almacenActualizado = almacen.copy(
                        nombreAlmacen = nuevoNombre,
                        capacidadMaxima = nuevaCapacidad
                    )

                    almacenManager.agregarOActualizarAlmacen(almacenActualizado)
                    actualizarAlmacenes()
                    actualizarResumenSaturacion()

                    Toast.makeText(this, "Almacén actualizado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Datos inválidos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEliminarAlmacen(almacen: AlmacenCapacidad) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Almacén")
            .setMessage("¿Está seguro de que desea eliminar el almacén '${almacen.nombreAlmacen}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                almacenManager.eliminarAlmacen(almacen.nombreAlmacen)
                actualizarAlmacenes()
                actualizarResumenSaturacion()
                Toast.makeText(this, "Almacén eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarAlmacenes() {
        try {
            val almacenes = almacenManager.obtenerAlmacenes()
            almacenAdapter.submitList(almacenes)

            // Mostrar/ocultar RecyclerView según si hay almacenes
            binding.recyclerAlmacenes.visibility = if (almacenes.isNotEmpty()) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }

            // Debug: Mostrar cuántos almacenes se cargaron
            Toast.makeText(this, "📊 Almacenes cargados: ${almacenes.size}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al actualizar almacenes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarResumenSaturacion() {
        try {
            val resumen = almacenManager.obtenerResumenTotal()

            binding.apply {
                tvTotalPalletsEscaneados.text = "Total Pallets Escaneados: ${resumen.totalPalletsEscaneados}"
                tvCapacidadTotalConfigurada.text = "Capacidad Total Configurada: ${resumen.totalCapacidadConfigurada}"
                tvSaturacionPromedio.text = "Saturación Promedio: ${resumen.saturacionPromedioFormateada}"

                // Mostrar/ocultar resumen según si hay almacenes configurados
                layoutResumenSaturacion.visibility = if (resumen.totalAlmacenes > 0) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al actualizar resumen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarResumenGrafico() {
        val pieChart = binding.pieChart
        val resumen = comparacion.groupingBy { it.estado }.eachCount()
        val entries = mutableListOf<PieEntry>()
        resumen.forEach { (estado, cantidad) ->
            entries.add(PieEntry(cantidad.toFloat(), estado))
        }
        val dataSet = PieDataSet(entries, "Resumen").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = Color.WHITE
            valueTextSize = 16f
        }
        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Resumen"
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun mostrarTablaDetalle() {
        val tabla = binding.tablaComparacion
        tabla.removeAllViews()
        // Header
        val header = TableRow(this)
        val headers = listOf("SKU", "Descripción", "Tarima", "Escaneado", "Inventario", "Diferencia", "Estado")
        headers.forEach {
            val tv = TextView(this)
            tv.text = it
            tv.setPadding(8, 8, 8, 8)
            tv.setBackgroundColor(Color.LTGRAY)
            header.addView(tv)
        }
        tabla.addView(header)
        // Rows
        comparacion.forEach { fila ->
            val row = TableRow(this)
            val datos = listOf(
                fila.sku,
                fila.descripcion,
                fila.tipoTarima,
                fila.escaneado?.toString() ?: "-",
                fila.inventario?.toString() ?: "-",
                fila.diferencia?.toString() ?: "-",
                fila.estado
            )
            datos.forEach {
                val tv = TextView(this)
                tv.text = it
                tv.setPadding(8, 8, 8, 8)
                row.addView(tv)
            }
            tabla.addView(row)
        }
    }

    private fun setupViews() {
        // Mostrar mensaje si no hay datos de comparación
        if (comparacion.isEmpty()) {
            binding.tvSinDatos.visibility = android.view.View.VISIBLE
            binding.pieChart.visibility = android.view.View.GONE
            binding.tablaComparacion.visibility = android.view.View.GONE
            binding.btnAlternarVista.visibility = android.view.View.GONE
        } else {
            binding.tvSinDatos.visibility = android.view.View.GONE
            binding.pieChart.visibility = android.view.View.VISIBLE
            binding.tablaComparacion.visibility = android.view.View.GONE
            binding.btnAlternarVista.visibility = android.view.View.VISIBLE
        }

        mostrarResumenGrafico()
        mostrarTablaDetalle()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar pallets escaneados cuando se regrese a esta actividad
        val totalPalletsActual = capturaViewModel.obtenerTotalPallets()
        val almacenActual = almacenManager.obtenerAlmacenActual()

        if (almacenActual != null) {
            almacenManager.actualizarPalletsEscaneados(almacenActual, totalPalletsActual)
            actualizarAlmacenes()
            actualizarResumenSaturacion()
        }
    }
}
