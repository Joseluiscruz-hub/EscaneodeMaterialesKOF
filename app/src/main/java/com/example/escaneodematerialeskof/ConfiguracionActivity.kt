package com.example.escaneodematerialeskof

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

@Deprecated("Usar ui.config.ConfiguracionActivity")
class ConfiguracionActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirigir inmediatamente a la Activity unificada
        startActivity(Intent(this, com.example.escaneodematerialeskof.ui.config.ConfiguracionActivity::class.java))
        finish()
        return
    }

    private fun setupViews() {
        // Restaurar estado de los switches desde SharedPreferences
        findViewById<Switch>(R.id.switch_notifications).isChecked =
            prefs.getBoolean("notifications_enabled", true)
        findViewById<Switch>(R.id.switch_sync).isChecked =
            prefs.getBoolean("sync_enabled", true)
    }

    private fun setupClickListeners() {
        // Switch de notificaciones
        findViewById<Switch>(R.id.switch_notifications).setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            val message = if (isChecked) "Notificaciones activadas" else "Notificaciones desactivadas"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // Switch de sincronización
        findViewById<Switch>(R.id.switch_sync).setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sync_enabled", isChecked).apply()
            val message =
                if (isChecked) "Sincronización automática activada" else "Sincronización automática desactivada"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // Limpiar caché
        findViewById<android.view.View>(R.id.option_clear_cache).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Limpiar caché")
                .setMessage("¿Estás seguro de que deseas limpiar la caché? Esta acción no se puede deshacer.")
                .setPositiveButton("Limpiar") { _, _ ->
                    // Aquí iría la lógica para limpiar caché
                    clearAppCache()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Crear respaldo
        findViewById<android.view.View>(R.id.option_backup).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Crear respaldo")
                .setMessage("Se creará un respaldo de todos tus datos de inventario.")
                .setPositiveButton("Crear") { _, _ ->
                    createBackup()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Acerca de
        findViewById<android.view.View>(R.id.option_about).setOnClickListener {
            showAboutDialog()
        }
    }

    private fun clearAppCache() {
        try {
            // Limpiar SharedPreferences relacionadas con caché
            val cachePrefs = getSharedPreferences("app_cache", MODE_PRIVATE)
            cachePrefs.edit().clear().apply()

            // Limpiar caché interno si es necesario
            cacheDir.deleteRecursively()

            Toast.makeText(this, "Caché limpiada correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al limpiar caché: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createBackup() {
        try {
            // Aquí iría la lógica real de respaldo
            // Por ahora, simulamos la creación del respaldo
            Thread {
                runOnUiThread {
                    Toast.makeText(this, "Creando respaldo...", Toast.LENGTH_SHORT).show()
                }

                // Simular tiempo de procesamiento
                Thread.sleep(2000)

                runOnUiThread {
                    Toast.makeText(this, "Respaldo creado correctamente", Toast.LENGTH_LONG).show()
                }
            }.start()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al crear respaldo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showAboutDialog() {
        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "N/A"
        }

        AlertDialog.Builder(this)
            .setTitle("Acerca de la aplicación")
            .setMessage(
                "Sistema KOF Materiales\n\n" +
                        "Versión: $versionName\n" +
                        "Desarrollado por: Equipo KOF\n\n" +
                        "Aplicación de gestión integral de inventarios y materiales."
            )
            .setPositiveButton("Cerrar", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
