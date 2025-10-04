package com.example.escaneodematerialeskof.ui.almacenes

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.escaneodematerialeskof.CapturaInventarioViewModel
import com.example.escaneodematerialeskof.databinding.ActivityGestionAlmacenesBinding
import com.example.escaneodematerialeskof.manager.AlmacenCapacidadManager
import com.example.escaneodematerialeskof.model.AlmacenCapacidad
import com.example.escaneodematerialeskof.ui.dashboard.AlmacenCapacidadAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Actividad dedicada exclusivamente a la gestión de capacidad de almacenes
 */
class GestionAlmacenesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionAlmacenesBinding
    private lateinit var almacenManager: AlmacenCapacidadManager
    private lateinit var almacenAdapter: AlmacenCapacidadAdapter
    private val capturaViewModel: CapturaInventarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionAlmacenesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Gestión de Almacenes"

        // Inicializar manager y adapter
        almacenManager = AlmacenCapacidadManager(this)
        setupRecyclerView()
        setupClickListeners()

        // Cargar datos iniciales
        actualizarAlmacenes()
        actualizarResumenSaturacion()

        // Mostrar mensaje de bienvenida
        Toast.makeText(this, "🏭 Gestión completa de capacidad de almacenes", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        almacenAdapter = AlmacenCapacidadAdapter(
            onEditarClick = { almacen -> mostrarDialogoEditarAlmacen(almacen) },
            onEliminarClick = { almacen -> mostrarDialogoEliminarAlmacen(almacen) }
        )

        binding.recyclerAlmacenes.apply {
            layoutManager = LinearLayoutManager(this@GestionAlmacenesActivity)
            adapter = almacenAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnConfigurarAlmacen.setOnClickListener {
            configurarNuevoAlmacen()
        }
    }

    private fun configurarNuevoAlmacen() {
        val nombreAlmacen = binding.etNombreAlmacen.text.toString().trim()
        val capacidadTexto = binding.etCapacidadMaxima.text.toString().trim()

        if (nombreAlmacen.isEmpty()) {
            Toast.makeText(this, "❌ Ingrese el nombre del almacén", Toast.LENGTH_SHORT).show()
            binding.etNombreAlmacen.requestFocus()
            return
        }

        if (capacidadTexto.isEmpty()) {
            Toast.makeText(this, "❌ Ingrese la capacidad máxima", Toast.LENGTH_SHORT).show()
            binding.etCapacidadMaxima.requestFocus()
            return
        }

        val capacidadMaxima = capacidadTexto.toIntOrNull()
        if (capacidadMaxima == null || capacidadMaxima <= 0) {
            Toast.makeText(this, "❌ Ingrese una capacidad válida (mayor a 0)", Toast.LENGTH_SHORT).show()
            binding.etCapacidadMaxima.requestFocus()
            return
        }

        // Verificar si el almacén ya existe
        val almacenesExistentes = almacenManager.obtenerAlmacenes()
        if (almacenesExistentes.any { it.nombreAlmacen.equals(nombreAlmacen, ignoreCase = true) }) {
            Toast.makeText(this, "⚠️ Ya existe un almacén con ese nombre", Toast.LENGTH_SHORT).show()
            binding.etNombreAlmacen.requestFocus()
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

        Toast.makeText(this, "✅ Almacén '$nombreAlmacen' configurado correctamente", Toast.LENGTH_LONG).show()
    }

    private fun mostrarDialogoEditarAlmacen(almacen: AlmacenCapacidad) {
        val etNombre = EditText(this).apply {
            setText(almacen.nombreAlmacen)
            hint = "Nombre del almacén"
            setTextColor(resources.getColor(android.R.color.black, null))
        }

        val etCapacidad = EditText(this).apply {
            setText(almacen.capacidadMaxima.toString())
            hint = "Capacidad máxima"
            inputType = InputType.TYPE_CLASS_NUMBER
            setTextColor(resources.getColor(android.R.color.black, null))
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
            addView(etNombre)
            addView(etCapacidad)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("✏️ Editar Almacén")
            .setMessage("Modifique los datos del almacén:")
            .setView(layout)
            .setPositiveButton("💾 Guardar") { _, _ ->
                val nuevoNombre = etNombre.text.toString().trim()
                val nuevaCapacidad = etCapacidad.text.toString().toIntOrNull()

                if (nuevoNombre.isEmpty()) {
                    Toast.makeText(this, "❌ El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (nuevaCapacidad == null || nuevaCapacidad <= 0) {
                    Toast.makeText(this, "❌ Ingrese una capacidad válida", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Verificar si el nuevo nombre ya existe (excepto el actual)
                if (nuevoNombre != almacen.nombreAlmacen) {
                    val almacenesExistentes = almacenManager.obtenerAlmacenes()
                    if (almacenesExistentes.any { it.nombreAlmacen.equals(nuevoNombre, ignoreCase = true) }) {
                        Toast.makeText(this, "⚠️ Ya existe un almacén con ese nombre", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    // Eliminar el almacén anterior si cambió el nombre
                    almacenManager.eliminarAlmacen(almacen.nombreAlmacen)
                }

                val almacenActualizado = almacen.copy(
                    nombreAlmacen = nuevoNombre,
                    capacidadMaxima = nuevaCapacidad
                )

                almacenManager.agregarOActualizarAlmacen(almacenActualizado)
                actualizarAlmacenes()
                actualizarResumenSaturacion()

                Toast.makeText(this, "✅ Almacén '$nuevoNombre' actualizado correctamente", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("❌ Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEliminarAlmacen(almacen: AlmacenCapacidad) {
        MaterialAlertDialogBuilder(this)
            .setTitle("🗑️ Eliminar Almacén")
            .setMessage("¿Está seguro de que desea eliminar el almacén '${almacen.nombreAlmacen}'?\n\n⚠️ Esta acción no se puede deshacer.\n\n📊 Datos actuales:\n• Capacidad: ${almacen.capacidadMaxima} pallets\n• Escaneados: ${almacen.palletsEscaneados} pallets\n• Saturación: ${almacen.porcentajeSaturacionFormateado}")
            .setPositiveButton("🗑️ Eliminar") { _, _ ->
                almacenManager.eliminarAlmacen(almacen.nombreAlmacen)
                actualizarAlmacenes()
                actualizarResumenSaturacion()
                Toast.makeText(this, "🗑️ Almacén '${almacen.nombreAlmacen}' eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("❌ Cancelar", null)
            .show()
    }

    private fun actualizarAlmacenes() {
        val almacenes = almacenManager.obtenerAlmacenes()
        almacenAdapter.submitList(almacenes)

        // Mostrar/ocultar elementos según si hay almacenes
        if (almacenes.isNotEmpty()) {
            binding.recyclerAlmacenes.visibility = View.VISIBLE
            binding.tvSinAlmacenes.visibility = View.GONE
            binding.layoutResumenSaturacion.visibility = View.VISIBLE
        } else {
            binding.recyclerAlmacenes.visibility = View.GONE
            binding.tvSinAlmacenes.visibility = View.VISIBLE
            binding.layoutResumenSaturacion.visibility = View.GONE
        }
    }

    private fun actualizarResumenSaturacion() {
        val resumen = almacenManager.obtenerResumenTotal()

        binding.apply {
            tvTotalPalletsEscaneados.text = getString(
                com.example.escaneodematerialeskof.R.string.label_pallets_escaneados,
                resumen.totalPalletsEscaneados
            )
            tvCapacidadTotalConfigurada.text = getString(
                com.example.escaneodematerialeskof.R.string.label_capacidad_total,
                resumen.totalCapacidadConfigurada
            )
            tvSaturacionPromedio.text = getString(
                com.example.escaneodematerialeskof.R.string.label_saturacion_promedio,
                resumen.saturacionPromedioFormateada
            )

            // Cambiar color del texto según saturación
            val saturacion = resumen.saturacionPromedio
            val colorResId = when {
                saturacion >= 90 -> android.R.color.holo_red_dark
                saturacion >= 75 -> android.R.color.holo_orange_dark
                saturacion >= 50 -> android.R.color.holo_blue_dark
                else -> android.R.color.holo_green_dark
            }
            tvSaturacionPromedio.setTextColor(getColor(colorResId))
        }
    }

    override fun onResume() {
        super.onResume()
        // Actualizar datos cuando se regrese a esta actividad
        val totalPalletsActual = capturaViewModel.obtenerTotalPallets()
        val almacenActual = almacenManager.obtenerAlmacenActual()

        if (almacenActual != null) {
            almacenManager.actualizarPalletsEscaneados(almacenActual, totalPalletsActual)
            actualizarAlmacenes()
            actualizarResumenSaturacion()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
