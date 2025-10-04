package com.example.escaneodematerialeskof.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.escaneodematerialeskof.*
import com.example.escaneodematerialeskof.ui.config.ConfiguracionActivity
import com.example.escaneodematerialeskof.ui.home.util.FrasesMotivacionales
import com.example.escaneodematerialeskof.ui.opciones.MasOpcionesActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView

class HomeNewActivity : AppCompatActivity() {

    // Vistas principales de la UI
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var txtFrase: TextView

    // Acciones principales
    private lateinit var cardCapturar: MaterialCardView
    private lateinit var btnResumen: MaterialButton
    private lateinit var btnComparar: MaterialButton

    // Lógica para frases motivacionales
    private var fraseIndex = 0
    private val frases = FrasesMotivacionales.all()
    private val fraseHandler = Handler(Looper.getMainLooper())
    private lateinit var fraseRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        // Configurar tema (claro/oscuro) antes de inflar la vista
        val sharedPreferences = getSharedPreferences("app_config", MODE_PRIVATE)
        val modoOscuro = sharedPreferences.getBoolean("modo_oscuro", false)
        AppCompatDelegate.setDefaultNightMode(
            if (modoOscuro) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_fixed)

        // Inicializar todas las vistas y listeners
        initializeViews()
        setupNavigationDrawer()
        setupListeners()
        setupFraseMotivacional()

        // Manejar el botón de retroceso para cerrar el menú lateral si está abierto
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
    }

    private fun initializeViews() {
        // Encontrar y asignar las vistas del layout
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)
        txtFrase = findViewById(R.id.txtFrase)
        cardCapturar = findViewById(R.id.card_capturar_inventario)
        btnResumen = findViewById(R.id.btn_resumen)
        btnComparar = findViewById(R.id.btn_comparar)
    }

    private fun setupNavigationDrawer() {
        // Configurar la Toolbar como la ActionBar de la actividad
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        // Sincronizar el DrawerLayout con la Toolbar
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Manejar los clics en los ítems del menú de navegación
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_inicio -> drawerLayout.closeDrawer(GravityCompat.START)
                R.id.nav_capturar_inventario -> showTipoEscaneoDialog()
                R.id.nav_dashboard, R.id.nav_resumen_inventario -> startActivity(
                    Intent(
                        this,
                        NewInventarioResumenActivity::class.java
                    )
                )

                R.id.nav_comparar_inventarios -> startActivity(Intent(this, InventoryComparisonActivity::class.java))
                R.id.nav_ajuste_inventario -> mostrarDialogoContrasena()
                R.id.nav_configuracion -> startActivity(Intent(this, ConfiguracionActivity::class.java))
                R.id.nav_mas_opciones -> startActivity(Intent(this, MasOpcionesActivity::class.java))
                R.id.nav_restos -> startActivity(Intent(this, RestosActivity::class.java))
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupListeners() {
        // Listener para la tarjeta principal de captura
        cardCapturar.setOnClickListener {
            showTipoEscaneoDialog()
        }

        // Listener para el botón de resumen
        btnResumen.setOnClickListener {
            startActivity(Intent(this, NewInventarioResumenActivity::class.java))
        }

        // Listener para el botón de comparar
        btnComparar.setOnClickListener {
            startActivity(Intent(this, InventoryComparisonActivity::class.java))
        }
    }

    private fun setupFraseMotivacional() {
        // Configura el runnable para cambiar la frase cada 4 segundos
        fraseRunnable = object : Runnable {
            override fun run() {
                txtFrase.text = frases[fraseIndex]
                fraseIndex = (fraseIndex + 1) % frases.size
                fraseHandler.postDelayed(this, 4000)
            }
        }
    }

    private fun showTipoEscaneoDialog() {
        val tipos = arrayOf("📦 Rumba", "🏗️ Pallet", "✋ Manual")
        MaterialAlertDialogBuilder(this, R.style.FemsaDialog)
            .setTitle("Seleccionar tipo de escaneo")
            .setItems(tipos) { _, which ->
                when (which) {
                    0 -> startCapturaInventario("rumba")
                    1 -> startCapturaInventario("pallet")
                    2 -> startCapturaInventario("manual")
                }
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun startCapturaInventario(tipo: String) {
        val intent = Intent(this, CapturaInventarioActivity::class.java)
        intent.putExtra(CapturaInventarioActivity.EXTRA_TIPO_ESCANEO, tipo)
        startActivity(intent)
    }

    private fun mostrarDialogoContrasena() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = getString(R.string.contrasena)
        }

        val prefs = getSharedPreferences("ajustes_inventario", MODE_PRIVATE)
        val contrasenaGuardada = prefs.getString("contrasena", null)

        MaterialAlertDialogBuilder(this, R.style.FemsaDialog)
            .setTitle(getString(R.string.ajustes_inventario))
            .setMessage(getString(R.string.confirmar_ajustes_inventario))
            .setView(input)
            .setPositiveButton(getString(R.string.aceptar)) { _, _ ->
                val contrasenaIngresada = input.text.toString()
                if (contrasenaGuardada != null && contrasenaIngresada == contrasenaGuardada) {
                    startActivity(Intent(this, AjusteInventarioActivity::class.java))
                } else {
                    val mensaje = if (contrasenaGuardada == null) {
                        "No hay contraseña configurada. Vaya a Configuración para establecer una."
                    } else {
                        getString(R.string.contrasena_incorrecta)
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Inicia el ciclo de frases motivacionales
        fraseHandler.post(fraseRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Detiene el ciclo para ahorrar recursos cuando la app no está visible
        fraseHandler.removeCallbacks(fraseRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpieza final para evitar fugas de memoria
        fraseHandler.removeCallbacksAndMessages(null)
    }
}
