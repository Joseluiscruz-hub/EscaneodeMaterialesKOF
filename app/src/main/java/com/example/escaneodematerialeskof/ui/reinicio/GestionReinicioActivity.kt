package com.example.escaneodematerialeskof.ui.reinicio

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.escaneodematerialeskof.R
import com.example.escaneodematerialeskof.viewmodel.InventoryComparisonViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.*

class GestionReinicioActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var tvEstadoSistema: TextView
    private lateinit var tvUltimoReinicio: TextView
    private lateinit var tvInventariosActivos: TextView
    private lateinit var tvHistorialReinicios: TextView // Cambiar de RecyclerView a TextView

    private lateinit var cardReiniciarComparacion: MaterialCardView
    private lateinit var cardReiniciarInventario: MaterialCardView
    private lateinit var cardLimpiarDatos: MaterialCardView
    private lateinit var cardReinicioCompleto: MaterialCardView

    private lateinit var inventoryComparisonViewModel: InventoryComparisonViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_reinicio)

        // Inicializar ViewModel y SharedPreferences
        inventoryComparisonViewModel = ViewModelProvider(this)[InventoryComparisonViewModel::class.java]
        sharedPreferences = getSharedPreferences("app_config", Context.MODE_PRIVATE)

        initializeViews()
        setupNavigationDrawer()
        setupClickListeners()
        updateSystemInfo()

        // Manejo de back con OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        tvEstadoSistema = findViewById(R.id.tv_estado_sistema)
        tvUltimoReinicio = findViewById(R.id.tv_estado_escaneado) // Reutilizar un TextView existente
        tvInventariosActivos = findViewById(R.id.tv_estado_comparacion) // Reutilizar un TextView existente
        tvHistorialReinicios = findViewById(R.id.tv_historial_reinicios) // Cambiar tipo a TextView

        cardReiniciarComparacion = findViewById(R.id.card_reiniciar_comparacion)
        cardReiniciarInventario = findViewById(R.id.card_reiniciar_escaneado)
        cardLimpiarDatos = findViewById(R.id.card_reiniciar_completo) // Reutilizar una card existente
        cardReinicioCompleto = findViewById(R.id.card_reiniciar_completo)

        // Configurar toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
    }

    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            // Manejar navegación aquí
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupClickListeners() {
        cardReiniciarComparacion.setOnClickListener {
            mostrarDialogoConfirmacion(
                titulo = getString(R.string.reinicio_comparacion),
                mensaje = getString(R.string.descripcion_reinicio_comparacion),
                accion = TipoReinicio.COMPARACION
            )
        }

        cardReiniciarInventario.setOnClickListener {
            mostrarDialogoConfirmacion(
                titulo = "📦 Reiniciar Inventario Escaneado",
                mensaje = "¿Estás seguro de que deseas reiniciar el inventario escaneado?\n\nEsto eliminará todos los datos escaneados pero mantendrá el inventario del sistema.",
                accion = TipoReinicio.INVENTARIO
            )
        }

        cardLimpiarDatos.setOnClickListener {
            mostrarDialogoConfirmacion(
                titulo = "🗑️ Limpiar Todos los Datos",
                mensaje = "¿Estás seguro de que deseas limpiar todos los datos?\n\nEsta acción eliminará tanto el inventario del sistema como los datos escaneados.",
                accion = TipoReinicio.DATOS
            )
        }

        cardReinicioCompleto.setOnClickListener {
            mostrarDialogoConfirmacion(
                titulo = getString(R.string.reinicio_completo),
                mensaje = getString(R.string.descripcion_reinicio_completo),
                accion = TipoReinicio.COMPLETO
            )
        }
    }

    private fun updateSystemInfo() {
        // Mostrar estado del sistema
        tvEstadoSistema.text = "Sistema funcionando correctamente"

        // Último reinicio
        val ultimoReinicio = sharedPreferences.getLong("ultimo_reinicio", 0L)
        if (ultimoReinicio > 0) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fechaFormateada = dateFormat.format(Date(ultimoReinicio))
            tvUltimoReinicio.text = "Último reinicio: $fechaFormateada"
        } else {
            tvUltimoReinicio.text = "Último reinicio: Nunca"
        }

        // Contar inventarios activos
        val inventariosActivos = obtenerInventariosActivos()
        tvInventariosActivos.text = "Inventarios activos: $inventariosActivos"
    }

    private fun obtenerInventariosActivos(): Int {
        // En una implementación real, esto contaría los inventarios activos
        // Por ahora, simulamos un número basado en preferencias
        val hayInventarioSistema = sharedPreferences.getBoolean("inventario_sistema_cargado", false)
        val hayInventarioEscaneado = sharedPreferences.getBoolean("inventario_escaneado_activo", false)

        var count = 0
        if (hayInventarioSistema) count++
        if (hayInventarioEscaneado) count++

        return count
    }

    private fun mostrarDialogoConfirmacion(titulo: String, mensaje: String, accion: TipoReinicio) {
        MaterialAlertDialogBuilder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton(getString(R.string.aceptar)) { _, _ ->
                ejecutarReinicio(accion)
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun ejecutarReinicio(tipo: TipoReinicio) {
        try {
            when (tipo) {
                TipoReinicio.COMPARACION -> {
                    // Reiniciar solo datos de comparación
                    inventoryComparisonViewModel.clearComparisonData()
                    sharedPreferences.edit().putBoolean("comparacion_activa", false).apply()
                }
                TipoReinicio.INVENTARIO -> {
                    // Reiniciar inventario escaneado
                    inventoryComparisonViewModel.clearScannedInventory()
                    sharedPreferences.edit().putBoolean("inventario_escaneado_activo", false).apply()
                }
                TipoReinicio.DATOS -> {
                    // Limpiar todos los datos pero mantener configuración
                    inventoryComparisonViewModel.clearAllInventoryData()
                    sharedPreferences.edit()
                        .putBoolean("inventario_sistema_cargado", false)
                        .putBoolean("inventario_escaneado_activo", false)
                        .putBoolean("comparacion_activa", false)
                        .apply()
                }
                TipoReinicio.COMPLETO -> {
                    // Reinicio completo del sistema
                    inventoryComparisonViewModel.resetSystem()
                    // Resetear todas las preferencias excepto el historial
                    val historial = sharedPreferences.getString("historial_reinicios", "[]")
                    sharedPreferences.edit().clear().apply()
                    sharedPreferences.edit().putString("historial_reinicios", historial).apply()
                }
            }

            // Actualizar timestamp del último reinicio
            val ahora = System.currentTimeMillis()
            sharedPreferences.edit().putLong("ultimo_reinicio", ahora).apply()

            // Registrar en el historial
            registrarReinicio(tipo, true)

            // Actualizar UI
            updateSystemInfo()
            Toast.makeText(this, getString(R.string.reinicio_exitoso), Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            // Registrar el error en el historial
            registrarReinicio(tipo, false)

            // Mostrar mensaje de error
            val mensajeError = getString(R.string.error_reinicio, e.message)
            Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show()
        }
    }

    private fun registrarReinicio(tipo: TipoReinicio, exito: Boolean) {
        // Crear nuevo item de historial
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaActual = dateFormat.format(Date())
        val usuario = sharedPreferences.getString("nombre_usuario", "Admin") ?: "Admin"

        val tipoTexto = when (tipo) {
            TipoReinicio.COMPARACION -> "🔄"
            TipoReinicio.INVENTARIO -> "📦"
            TipoReinicio.DATOS -> "🗑️"
            TipoReinicio.COMPLETO -> "⚠️"
        }

        val tituloTexto = when (tipo) {
            TipoReinicio.COMPARACION -> "Reinicio de Comparación"
            TipoReinicio.INVENTARIO -> "Reinicio de Inventario"
            TipoReinicio.DATOS -> "Limpieza de Datos"
            TipoReinicio.COMPLETO -> "Reinicio Completo"
        }

        val nuevoItem = HistorialReinicioItem(
            tipo = tipoTexto,
            titulo = tituloTexto,
            fecha = fechaActual,
            usuario = usuario,
            exito = exito
        )

        // Obtener historial actual y agregar el nuevo
        val historialActual = obtenerHistorialReinicios().toMutableList()
        historialActual.add(0, nuevoItem) // Agregar al inicio para que el más reciente esté primero

        // Limitar a los últimos 20 registros
        val historialLimitado = if (historialActual.size > 20) {
            historialActual.subList(0, 20)
        } else {
            historialActual
        }

        // Guardar en SharedPreferences (en una implementación real usaríamos Room o Firestore)
        // Por simplicidad, convertimos a una cadena separada por delimitadores
        val historialString = historialLimitado.joinToString("|") { item ->
            "${item.tipo}~${item.titulo}~${item.fecha}~${item.usuario}~${item.exito}"
        }

        sharedPreferences.edit().putString("historial_reinicios", historialString).apply()

        // Actualizar el TextView de historial
        cargarHistorialReinicios()
    }

    private fun obtenerHistorialReinicios(): List<HistorialReinicioItem> {
        val historialString = sharedPreferences.getString("historial_reinicios", "") ?: ""
        if (historialString.isEmpty()) return emptyList()

        return historialString.split("|").mapNotNull { itemString ->
            val partes = itemString.split("~")
            if (partes.size >= 5) {
                HistorialReinicioItem(
                    tipo = partes[0],
                    titulo = partes[1],
                    fecha = partes[2],
                    usuario = partes[3],
                    exito = partes[4].toBoolean()
                )
            } else null
        }
    }

    private fun cargarHistorialReinicios() {
        val historial = obtenerHistorialReinicios()
        // Actualizar el TextView con el historial
        tvHistorialReinicios.text = historial.joinToString("\n") { "${it.fecha} - ${it.titulo} (${if (it.exito) "Éxito" else "Error"})" }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }
}
