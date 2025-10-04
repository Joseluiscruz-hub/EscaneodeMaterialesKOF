package com.example.escaneodematerialeskof

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class MainDashboardPremiumActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_dashboard_premium)

        // Inicializar vistas
        initializeViews()

        setupNavigationDrawer()
        setupClickListeners()

        // Manejo de back con OnBackPressedDispatcher en lugar de onBackPressed()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Delegar al dispatcher para comportamiento por defecto
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        // El Toolbar no está en el nuevo layout, así que se elimina su inicialización.
    }

    private fun setupNavigationDrawer() {
        // El Toolbar no está, así que creamos un Toggle simple.
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home, R.id.nav_inicio -> { /* ya en home */
                }

                R.id.nav_inventory, R.id.nav_resumen_inventario -> startActivity(
                    Intent(this, com.example.escaneodematerialeskof.ui.resumen.InventarioResumenActivity::class.java)
                )

                R.id.nav_capture, R.id.nav_capturar_inventario -> startActivity(
                    Intent(this, CapturaInventarioActivity::class.java)
                )

                R.id.nav_comparison, R.id.nav_comparar_inventarios -> startActivity(
                    Intent(this, InventoryComparisonActivity::class.java)
                )

                R.id.nav_comparacion_tiempo_real -> startActivity(
                    Intent(this, com.example.escaneodematerialeskof.ui.comparacion.ComparacionTiempoRealActivity::class.java)
                )

                R.id.nav_restos -> startActivity(Intent(this, RestosActivity::class.java))
                R.id.nav_settings, R.id.nav_configuracion -> startActivity(
                    Intent(this, com.example.escaneodematerialeskof.ui.config.ConfiguracionActivity::class.java)
                )

                R.id.nav_ajuste_inventario -> startActivity(Intent(this, AjusteInventarioActivity::class.java))
                R.id.nav_gestion_almacenes -> startActivity(
                    Intent(this, com.example.escaneodematerialeskof.ui.almacenes.GestionAlmacenesActivity::class.java)
                )

                R.id.nav_mas_opciones -> startActivity(
                    Intent(this, com.example.escaneodematerialeskof.ui.opciones.MasOpcionesActivity::class.java)
                )

                R.id.nav_about -> showAboutDialog()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupClickListeners() {
        findViewById<CardView>(R.id.card_capturar_inventario).setOnClickListener {
            startActivity(Intent(this, CapturaInventarioActivity::class.java))
        }
        findViewById<CardView>(R.id.card_dashboard).setOnClickListener {
            // Abrir dashboard compose (versión ejecutiva)
            startActivity(Intent(this, com.example.escaneodematerialeskof.ui.dashboard.DashboardComposeActivity::class.java))
        }
        findViewById<CardView>(R.id.card_resumen).setOnClickListener {
            startActivity(Intent(this, com.example.escaneodematerialeskof.ui.resumen.InventarioResumenActivity::class.java))
        }
        findViewById<CardView>(R.id.card_comparar).setOnClickListener {
            // Diálogo para elegir tipo de comparación
            val opciones = arrayOf(
                getString(R.string.comparacion_archivos_opcion),
                getString(R.string.comparar_tiempo_real) // ya existe string
            )
            MaterialAlertDialogBuilder(this, R.style.FemsaDialog)
                .setTitle(getString(R.string.opciones_comparacion))
                .setItems(opciones) { d, which ->
                    when (which) {
                        0 -> startActivity(Intent(this, InventoryComparisonActivity::class.java))
                        1 -> startActivity(Intent(this, com.example.escaneodematerialeskof.ui.comparacion.ComparacionTiempoRealActivity::class.java))
                    }
                    d.dismiss()
                }
                .setNegativeButton(R.string.cancelar, null)
                .show()
        }
        findViewById<CardView>(R.id.card_importar).setOnClickListener {
            startActivity(Intent(this, ImportarActivity::class.java))
        }
        findViewById<CardView>(R.id.card_configuracion).setOnClickListener {
            startActivity(Intent(this, com.example.escaneodematerialeskof.ui.config.ConfiguracionActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.card_ajuste).setOnClickListener {
            startActivity(Intent(this, AjusteInventarioActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btn_gestion_almacenes).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    com.example.escaneodematerialeskof.ui.almacenes.GestionAlmacenesActivity::class.java
                )
            )
        }
    }

    private fun showAboutDialog() {
        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "N/A" // Valor predeterminado en caso de error
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.about_title))
            .setMessage(
                getString(R.string.about_description) + "\n\n" +
                        getString(R.string.about_version_label) + ": " + versionName + "\n" +
                        getString(R.string.about_developer)
            )
            .setPositiveButton(getString(R.string.about_close), null)
            .show()
    }
}
