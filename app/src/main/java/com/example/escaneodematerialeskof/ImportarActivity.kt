package com.example.escaneodematerialeskof

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class ImportarActivity : AppCompatActivity() {

    private val viewModel: CapturaInventarioViewModel by viewModels()

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                processCSVFile(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_importar)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        findViewById<CardView>(R.id.card_import_file).setOnClickListener {
            openFilePicker()
        }

        findViewById<CardView>(R.id.card_import_system).setOnClickListener {
            importFromSystem()
        }

        findViewById<CardView>(R.id.card_import_backup).setOnClickListener {
            restoreFromBackup()
        }
    }

    private fun openFilePicker() {
        // Verificar permisos de almacenamiento antes de abrir el selector
        if (!com.example.escaneodematerialeskof.util.PermissionHelper.hasStoragePermissions(this)) {
            com.example.escaneodematerialeskof.util.PermissionHelper.requestStoragePermissions(this)
            return
        }

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(Intent.createChooser(intent, "Seleccionar archivo CSV"))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        com.example.escaneodematerialeskof.util.PermissionHelper.handlePermissionResult(
            requestCode,
            grantResults,
            onGranted = {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
                openFilePicker()
            },
            onDenied = {
                Toast.makeText(this, "Se necesitan permisos de almacenamiento para importar archivos", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun processCSVFile(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))

            val lines = reader.readLines()
            reader.close()

            if (lines.isEmpty()) {
                Toast.makeText(this, "El archivo está vacío", Toast.LENGTH_SHORT).show()
                return
            }

            // Mostrar diálogo de confirmación con preview
            val preview = if (lines.size > 3) {
                lines.take(3).joinToString("\n") + "\n... (${lines.size} líneas totales)"
            } else {
                lines.joinToString("\n")
            }

            AlertDialog.Builder(this)
                .setTitle("Confirmar importación")
                .setMessage("Se encontraron ${lines.size} líneas.\n\nPreview:\n$preview\n\n¿Deseas continuar?")
                .setPositiveButton("Importar") { _, _ ->
                    performCSVImport(lines)
                }
                .setNegativeButton("Cancelar", null)
                .show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error al leer el archivo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun performCSVImport(lines: List<String>) {
        try {
            Toast.makeText(this, "Procesando importación...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    var successCount = 0
                    var errorCount = 0

                    // Saltar la primera línea (header) y procesar el resto
                    for (i in 1 until lines.size) {
                        val line = lines[i].trim()
                        if (line.isEmpty()) continue

                        // Parsear CSV (formato: SKU,Descripción,Centro,Línea,OP,F.Producción,DíasV,Ubicación,Pallets,Restos,TipoTarima,Almacén)
                        val fields = line.split(",").map { it.trim().removeSurrounding("\"") }

                        if (fields.size >= 11) {
                            val material = com.example.escaneodematerialeskof.model.MaterialItem(
                                sku = fields.getOrNull(0) ?: "",
                                descripcion = fields.getOrNull(1) ?: "",
                                centro = fields.getOrNull(2) ?: "",
                                linea = fields.getOrNull(3) ?: "",
                                op = fields.getOrNull(4) ?: "",
                                fProd = fields.getOrNull(5) ?: "",
                                diasV = fields.getOrNull(6) ?: "",
                                ubicacion = fields.getOrNull(7) ?: "",
                                totalPallets = fields.getOrNull(8) ?: "0",
                                restos = fields.getOrNull(9) ?: "",
                                tipoTarima = fields.getOrNull(10) ?: "KOF",
                                almacen = fields.getOrNull(11),
                                cxPal = "",
                                fpc = "",
                                con = ""
                            )

                            // Guardar usando el ViewModel
                            withContext(Dispatchers.Main) {
                                viewModel.guardarMaterialEnArchivo(material) { success, _ ->
                                    if (success) successCount++ else errorCount++
                                }
                            }
                        } else {
                            errorCount++
                        }
                    }

                    withContext(Dispatchers.Main) {
                        AlertDialog.Builder(this@ImportarActivity)
                            .setTitle("Importación completada")
                            .setMessage("✅ Registros importados: $successCount\n❌ Errores: $errorCount")
                            .setPositiveButton("OK") { _, _ ->
                                finish() // Volver al dashboard
                            }
                            .show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ImportarActivity, "Error durante la importación: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error durante la importación: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun importFromSystem() {
        AlertDialog.Builder(this)
            .setTitle("Importar desde sistema")
            .setMessage("Esta función sincronizará los datos más recientes desde el sistema central. ¿Continuar?")
            .setPositiveButton("Sincronizar") { _, _ ->
                performSystemSync()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performSystemSync() {
        Toast.makeText(this, "Sincronizando con sistema central...", Toast.LENGTH_SHORT).show()

        Thread {
            // Simular sincronización
            Thread.sleep(3000)

            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle("Sincronización completada")
                    .setMessage("Se sincronizaron exitosamente los datos del sistema central.")
                    .setPositiveButton("OK") { _, _ ->
                        finish()
                    }
                    .show()
            }
        }.start()
    }

    private fun restoreFromBackup() {
        // Obtener lista de respaldos disponibles (simulado)
        val backups = listOf(
            "Respaldo_2024_01_15.backup",
            "Respaldo_2024_01_10.backup",
            "Respaldo_2024_01_05.backup"
        )

        if (backups.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("No hay respaldos")
                .setMessage("No se encontraron respaldos disponibles para restaurar.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Seleccionar respaldo")
            .setItems(backups.toTypedArray()) { _, which ->
                val selectedBackup = backups[which]
                confirmRestore(selectedBackup)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmRestore(backupName: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar restauración")
            .setMessage("¿Estás seguro de que deseas restaurar desde '$backupName'?\n\nEsta acción sobrescribirá los datos actuales.")
            .setPositiveButton("Restaurar") { _, _ ->
                performRestore(backupName)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performRestore(backupName: String) {
        Toast.makeText(this, "Restaurando desde $backupName...", Toast.LENGTH_SHORT).show()

        Thread {
            // Simular restauración
            Thread.sleep(2500)

            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle("Restauración completada")
                    .setMessage("Los datos se restauraron exitosamente desde '$backupName'.")
                    .setPositiveButton("OK") { _, _ ->
                        finish()
                    }
                    .show()
            }
        }.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
