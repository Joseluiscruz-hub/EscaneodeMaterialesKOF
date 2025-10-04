package com.example.escaneodematerialeskof.ui.config

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import com.example.escaneodematerialeskof.R
import com.example.escaneodematerialeskof.data.AppDatabase
import com.example.escaneodematerialeskof.databinding.ActivityConfiguracionBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
/**
 * Activity for application configuration settings.
 * Allows users to customize app behavior and preferences.
 */
class ConfiguracionActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    // UI Elements
    private lateinit var switchNotificaciones: Switch
    private lateinit var switchModoOscuro: Switch
    private lateinit var switchGuardarHistorial: Switch
    private lateinit var etTiempoEscaneo: TextInputEditText
    private lateinit var rbRumba: RadioButton
    private lateinit var rbPallet: RadioButton
    private lateinit var rbManual: RadioButton
    private lateinit var tvVersionApp: TextView
    private lateinit var btnLimpiarDatos: Button
    private lateinit var btnGuardarConfiguracion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("app_config", Context.MODE_PRIVATE)

        // Apply current theme before creating UI
        val dark = sharedPreferences.getBoolean("modo_oscuro", false)
        if (dark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Usar View Binding con layout XML y SettingsFragment
        val binding = ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    private fun createUI() {
        // Create the main ScrollView
        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Create the main LinearLayout
        val mainLayout = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(32) // 16dp padding
        }

        // Add title
        val titleText = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32 // 16dp margin
            }
            text = "Configuración"
            textSize = 24f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        mainLayout.addView(titleText)

        // General Configuration Card
        val generalCard = createCard()
        val generalLayout = createCardLayout()

        val generalTitle = createSectionTitle("Configuración General")
        generalLayout.addView(generalTitle)

        switchNotificaciones = Switch(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Notificaciones"
        }
        generalLayout.addView(switchNotificaciones)

        switchModoOscuro = Switch(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Modo Oscuro"
        }
        generalLayout.addView(switchModoOscuro)

        switchGuardarHistorial = Switch(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Guardar Historial de Escaneos"
        }
        generalLayout.addView(switchGuardarHistorial)

        generalCard.addView(generalLayout)
        mainLayout.addView(generalCard)

        // Scan Configuration Card
        val scanCard = createCard()
        val scanLayout = createCardLayout()

        val scanTitle = createSectionTitle("Configuración de Escaneo")
        scanLayout.addView(scanTitle)

        val inputLayout = TextInputLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            hint = "Tiempo de escaneo (segundos)"
        }

        etTiempoEscaneo = TextInputEditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setText("5")
        }
        inputLayout.addView(etTiempoEscaneo)
        scanLayout.addView(inputLayout)

        val scanTypeLabel = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
            text = "Tipo de escaneo predeterminado:"
        }
        scanLayout.addView(scanTypeLabel)

        val radioGroup = RadioGroup(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        }

        rbRumba = RadioButton(this).apply {
            layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            )
            text = "Rumba"
        }
        radioGroup.addView(rbRumba)

        rbPallet = RadioButton(this).apply {
            layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            )
            text = "Pallet"
        }
        radioGroup.addView(rbPallet)

        rbManual = RadioButton(this).apply {
            layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            )
            text = "Manual"
        }
        radioGroup.addView(rbManual)

        scanLayout.addView(radioGroup)
        scanCard.addView(scanLayout)
        mainLayout.addView(scanCard)

        // App Info Card
        val infoCard = createCard()
        val infoLayout = createCardLayout()

        val infoTitle = createSectionTitle("Información de la Aplicación")
        infoLayout.addView(infoTitle)

        tvVersionApp = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Versión: 1.0.0"
        }
        infoLayout.addView(tvVersionApp)

        btnLimpiarDatos = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Limpiar Datos de la Aplicación"
            setBackgroundColor(Color.parseColor("#F44336"))
            setTextColor(Color.WHITE)
        }
        infoLayout.addView(btnLimpiarDatos)

        infoCard.addView(infoLayout)
        mainLayout.addView(infoCard)

        // Save Button
        btnGuardarConfiguracion = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 32
            }
            text = "Guardar Configuración"
            setBackgroundColor(Color.parseColor("#2196F3")) // Material Blue
            setTextColor(Color.WHITE)
        }
        mainLayout.addView(btnGuardarConfiguracion)

        // Set app version
        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            tvVersionApp.text = "Versión: $versionName"
        } catch (e: Exception) {
            tvVersionApp.text = "Versión: 1.0.0"
        }

        // Add the main layout to the scroll view
        scrollView.addView(mainLayout)

        // Set the content view
        setContentView(scrollView)
    }

    private fun createCard(): CardView {
        return CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32
            }
            radius = 16f
            cardElevation = 8f
        }
    }

    private fun createCardLayout(): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(32)
        }
    }

    private fun createSectionTitle(title: String): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = title
            textSize = 18f
            setTextColor(Color.BLACK)
        }
    }

    private fun loadPreferences() {
        // Load saved preferences and update UI
        switchNotificaciones.isChecked = sharedPreferences.getBoolean("notificaciones", true)
        switchModoOscuro.isChecked = sharedPreferences.getBoolean("modo_oscuro", false)
        switchGuardarHistorial.isChecked = sharedPreferences.getBoolean("guardar_historial", true)

        etTiempoEscaneo.setText(sharedPreferences.getInt("tiempo_escaneo", 5).toString())

        // Set default scan type
        when (sharedPreferences.getString("tipo_escaneo_default", "rumba")) {
            "rumba" -> rbRumba.isChecked = true
            "pallet" -> rbPallet.isChecked = true
            "manual" -> rbManual.isChecked = true
            else -> rbRumba.isChecked = true
        }

        // Apply dark mode immediately based on saved preference
        AppCompatDelegate.setDefaultNightMode(
            if (switchModoOscuro.isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun setupClickListeners() {
        btnGuardarConfiguracion.setOnClickListener {
            savePreferences()
            Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Apply dark mode instantly when toggled and persist the value
        switchModoOscuro.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("modo_oscuro", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        btnLimpiarDatos.setOnClickListener {
            showConfirmationDialog()
        }
    }

    private fun savePreferences() {
        val editor = sharedPreferences.edit()

        // Save switches state
        editor.putBoolean("notificaciones", switchNotificaciones.isChecked)
        editor.putBoolean("modo_oscuro", switchModoOscuro.isChecked)
        editor.putBoolean("guardar_historial", switchGuardarHistorial.isChecked)

        // Save scan time with basic validation (min 1, max 60)
        var tiempoEscaneo = etTiempoEscaneo.text.toString().toIntOrNull() ?: 5
        if (tiempoEscaneo < 1) tiempoEscaneo = 1
        if (tiempoEscaneo > 60) tiempoEscaneo = 60
        editor.putInt("tiempo_escaneo", tiempoEscaneo)

        // Save default scan type
        val tipoEscaneoDefault = when {
            rbRumba.isChecked -> "rumba"
            rbPallet.isChecked -> "pallet"
            rbManual.isChecked -> "manual"
            else -> "rumba"
        }
        editor.putString("tipo_escaneo_default", tipoEscaneoDefault)

        editor.apply()

        // Apply dark mode immediately
        if (switchModoOscuro.isChecked) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Limpiar Datos")
            .setMessage("¿Estás seguro de que deseas limpiar todos los datos de la aplicación? Esta acción no se puede deshacer.")
            .setPositiveButton("Sí") { _, _ ->
                clearAppData()
                Toast.makeText(this, "Datos limpiados correctamente", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun clearAppData() {
        // Clear SharedPreferences
        sharedPreferences.edit().clear().apply()

        // Clear Room database tables
        try {
            AppDatabase.getDatabase(this).clearAllTables()
        } catch (e: Exception) {
            // Ignore errors but inform user via toast upon caller
        }

        // Reset UI to defaults
        loadPreferences()
    }
}
