@file:Suppress("DEPRECATION")

package com.example.escaneodematerialeskof

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.escaneodematerialeskof.databinding.ActivityInventarioBinding
import com.example.escaneodematerialeskof.manager.AlmacenCapacidadManager
import com.example.escaneodematerialeskof.model.AlmacenCapacidad
import com.example.escaneodematerialeskof.model.MaterialItem
import com.example.escaneodematerialeskof.util.QrParser
import com.example.escaneodematerialeskof.util.ScanMode
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.File

/**
 * Actividad principal para la captura de inventario.
 * Permite escanear códigos QR, guardar materiales en un archivo CSV,
 * mostrar una tabla acumulada de materiales, exportar e importar datos,
 * y enviar el inventario por correo.
 */
class CapturaInventarioActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TIPO_ESCANEO = "tipo_escaneo"
    }

    private lateinit var binding: ActivityInventarioBinding
    private val viewModel: CapturaInventarioViewModel by viewModels()

    private val scanLauncher: androidx.activity.result.ActivityResultLauncher<ScanOptions> =
        registerForActivityResult(ScanContract()) { result: com.journeyapps.barcodescanner.ScanIntentResult? ->
            if (result?.contents == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show()
            } else {
                val scanMode =
                    ScanMode.fromString(viewModel.modoEscaneo.value ?: intent.getStringExtra(EXTRA_TIPO_ESCANEO))
                when (scanMode) {
                    is ScanMode.Pallet -> escaneoPalletContinuo(result.contents)
                    is ScanMode.Rumba -> escaneoRumba(result.contents)
                    is ScanMode.Manual -> {
                        binding.etSKU.setText("")
                        Toast.makeText(this, "Modo MANUAL: Ingrese SKU, DP y FPC manualmente", Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> {
                        val qrText = result.contents
                        try {
                            val datos = QrParser.parse(qrText)
                            binding.etSKU.setText(datos["SKU"] ?: "")
                            binding.etDP.setText(datos["DP"] ?: "")
                            binding.etCxPal.setText(datos["CxPal"] ?: "")
                            binding.etFPC.setText(datos["FPC"] ?: "")
                            binding.etCon.setText(datos["Con"] ?: "")
                            binding.etCentro.setText(datos["Centro"] ?: "")
                            binding.etLINEA.setText(datos["LINEA"] ?: "")
                            binding.etOP.setText(datos["OP"] ?: "")
                            binding.etFProd.setText(datos["FProd"] ?: "")
                            binding.etDiasV.setText(datos["Dias V"] ?: "")
                            Toast.makeText(this, getString(R.string.toast_datos_qr_extraidos), Toast.LENGTH_SHORT)
                                .show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error al procesar el código QR: ${e.message}", Toast.LENGTH_LONG)
                                .show()
                            limpiarCampos()
                        }
                    }
                }
            }
        }

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) exportarCSVAcumulado(uri)
    }
    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) cargarCSVAcumulado(uri)
    }

    private var lastGuardarClick = 0L
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        almacenManager = AlmacenCapacidadManager(this)
        mostrarDialogoSeleccionAlmacen()

        val requestCameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                iniciarEscaneoQR()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_LONG).show()
            }
        }

        prefs = getSharedPreferences("captura_prefs", MODE_PRIVATE)
        val modoInicialIntent = (intent.getStringExtra(EXTRA_TIPO_ESCANEO) ?: "").lowercase()
        val modoPersistido = prefs.getString("ultimo_modo", null)
        val tipoEscaneo = (modoInicialIntent.ifBlank { modoPersistido } ?: "manual").lowercase()
        val tvTitulo = binding.tvTitulo
        viewModel.setModoEscaneo(tipoEscaneo)
        if (modoPersistido == null && modoInicialIntent.isNotBlank()) {
            prefs.edit().putString("ultimo_modo", modoInicialIntent.lowercase()).apply()
        }
        // Restaurar última tarima seleccionada
        tipoTarimaSeleccionada = prefs.getString("ultima_tarima", null)

        val scanMode = ScanMode.fromString(tipoEscaneo)
        when (scanMode) {
            is ScanMode.Rumba -> configurarModoRumba(tvTitulo)
            is ScanMode.Pallet -> configurarModoPallet(tvTitulo)
            is ScanMode.Manual -> configurarModoManual(tvTitulo)
            else -> configurarModoDefault(tvTitulo)
        }

        prefs.edit().putString("ultima_tarima", tipoTarimaSeleccionada ?: "KOF").apply()
        binding.btnEscanearGrande.setOnClickListener {
            if (tienePermisoCamara()) {
                val modos = arrayOf("Rumba", "Pallet", "Manual")
                var idxSeleccionado = 0
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.seleccionar_tipo_escaneo))
                    .setSingleChoiceItems(modos, 0) { _, which -> idxSeleccionado = which }
                    .setPositiveButton(getString(R.string.aceptar)) { dialog, _ ->
                        dialog.dismiss()
                        when (idxSeleccionado) {
                            0 -> {
                                viewModel.setModoEscaneo("rumba")
                                prefs.edit().putString("ultimo_modo", "rumba").apply()
                                tvTitulo.text = getString(R.string.captura_rumba)
                                iniciarEscaneoQR()
                            }

                            1 -> {
                                viewModel.setModoEscaneo("pallet")
                                prefs.edit().putString("ultimo_modo", "pallet").apply()
                                mostrarSelectorTarima { seleccion ->
                                    tipoTarimaSeleccionada = seleccion
                                    prefs.edit().putString("ultima_tarima", seleccion).apply()
                                    tvTitulo.text = getString(R.string.captura_pallets)
                                    iniciarEscaneoQR()
                                }
                            }

                            2 -> {
                                viewModel.setModoEscaneo("manual")
                                prefs.edit().putString("ultimo_modo", "manual").apply()
                                tvTitulo.text = getString(R.string.captura_manual)
                                Toast.makeText(
                                    this,
                                    "Modo MANUAL: Ingrese datos y presione Guardar",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton(getString(R.string.cancelar), null)
                    .show()
            } else requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.btnGuardar.setOnClickListener {
            if (binding.etSKU.text.isNullOrBlank() && !binding.etSkuManual.text.isNullOrBlank()) {
                binding.etSKU.setText(binding.etSkuManual.text.toString())
            }
            if (SystemClock.elapsedRealtime() - lastGuardarClick < 600) return@setOnClickListener
            lastGuardarClick = SystemClock.elapsedRealtime()
            if (!validarCamposObligatorios()) return@setOnClickListener
            val material = crearMaterialDesdeUI()
            viewModel.guardarMaterialEnArchivo(material) { success, message ->
                runOnUiThread {
                    if (success) {
                        viewModel.registrarTarima(
                            material.sku,
                            material.totalPallets?.toIntOrNull() ?: 1,
                            tipoTarimaSeleccionada
                        )
                        mostrarAcumuladoQE()
                        viewModel.actualizarCapacidadAlmacen()
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        viewModel.enviarMaterialAlServidor(material) { enviado, msg ->
                            runOnUiThread {
                                if (enviado) {
                                    Toast.makeText(this, "Enviado a servidor: $msg", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Error al enviar a servidor: $msg", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        limpiarCampos()
                    } else {
                        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnSiguiente.setOnClickListener {
            limpiarCampos()
            Toast.makeText(this, getString(R.string.toast_siguiente_material), Toast.LENGTH_SHORT).show()
        }

        binding.btnExportar.setOnClickListener {
            exportLauncher.launch("export_inventario_acumulado_${System.currentTimeMillis()}.csv")
        }

        binding.btnCargar.setOnClickListener {
            importLauncher.launch("text/csv")
        }

        binding.btnEnviarCorreo.setOnClickListener { enviarInventarioPorCorreo() }

        binding.btnScanRumba.setOnClickListener {
            mostrarSelectorTarima { seleccion ->
                tipoTarimaSeleccionada = seleccion
                prefs.edit().putString("ultima_tarima", seleccion).apply()
                tvTitulo.text = getString(R.string.captura_rumba)
                intent.putExtra(EXTRA_TIPO_ESCANEO, "rumba")
                iniciarEscaneoQR()
            }
        }

        binding.btnScanPallets.setOnClickListener {
            mostrarSelectorTarima { seleccion ->
                tipoTarimaSeleccionada = seleccion
                prefs.edit().putString("ultima_tarima", seleccion).apply()
                tvTitulo.text = getString(R.string.captura_pallets)
                intent.putExtra(EXTRA_TIPO_ESCANEO, "pallet")
                val options = ScanOptions().apply {
                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    setPrompt(getString(R.string.scan_prompt))
                    setCameraId(0)
                    setBeepEnabled(true)
                    setBarcodeImageEnabled(true)
                }
                scanLauncher.launch(options)
            }
        }

        binding.btnScanManual.setOnClickListener {
            intent.putExtra(EXTRA_TIPO_ESCANEO, "manual")
            viewModel.setModoEscaneo("manual")
            prefs.edit().putString("ultimo_modo", "manual").apply()
            recreate()
        }

        binding.btnRestos.setOnClickListener {
            val intent = Intent(this, RestosActivity::class.java)
            startActivity(intent)
        }

        binding.btnAbrirResumen.setOnClickListener {
            val intent = Intent(this, NewInventarioResumenActivity::class.java)
            intent.putExtra("mostrarGrafica", true)
            startActivity(intent)
        }

        binding.btnCompararEnviar.setOnClickListener { btn ->
            btn.animate().alpha(0.6f).setDuration(100).withEndAction {
                btn.animate().alpha(1f).setDuration(100).start()
            }.start()

            val progressDialog = AlertDialog.Builder(this)
                .setTitle("Comparando inventarios")
                .setMessage("Procesando datos, por favor espere...")
                .setCancelable(false)
                .create()
            progressDialog.show()

            viewModel.compararInventarios { success, message, comparacion ->
                progressDialog.dismiss()
                if (success && comparacion != null) {
                    AlertDialog.Builder(this)
                        .setTitle("Resumen de comparación")
                        .setMessage(message)
                        .setPositiveButton("Ver detalles") { _, _ ->
                            mostrarDetallesComparacion(comparacion)
                        }
                        .setNeutralButton("Enviar") { _, _ ->
                            com.example.escaneodematerialeskof.dashboard.ComparadorInventario.exportarComparacionCSV(
                                this,
                                comparacion
                            ) { archivo ->
                                if (archivo != null) {
                                    com.example.escaneodematerialeskof.dashboard.ComparadorInventario.enviarPorCorreo(
                                        this,
                                        archivo
                                    )
                                } else {
                                    Toast.makeText(this, "No se pudo generar el archivo", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .setNegativeButton(getString(R.string.cancelar), null)
                        .show()
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
        }

        mostrarAcumuladoQE()
        // Ejecutar envío por correo automático si se solicitó
        if (intent.getBooleanExtra("enviar_por_correo", false)) {
            enviarInventarioPorCorreo()
        }
    } // <- cierre correcto de onCreate

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_inventario_acciones, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_exportar -> {
                binding.btnExportar.performClick(); true
            }

            R.id.action_reset_inventario -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.reset_inventario)
                    .setMessage(R.string.confirm_reset_inventario)
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        resetearInventario()
                        Toast.makeText(this, getString(R.string.toast_inventario_reseteado), Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show(); true
            }

            R.id.action_add -> {
                binding.btnGuardar.performClick(); true
            }

            R.id.action_delete -> {
                AlertDialog.Builder(this)
                    .setTitle("Eliminar último registro")
                    .setMessage("¿Deseas eliminar el último material guardado?")
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        viewModel.eliminarUltimoMaterialGuardado { ok, msg ->
                            runOnUiThread {
                                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                                if (ok) mostrarAcumuladoQE()
                            }
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show(); true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun enviarInventarioPorCorreo() {
        try {
            val file = File(filesDir, "materiales_guardados.csv")
            if (!file.exists()) {
                Toast.makeText(this, getString(R.string.toast_no_inventario), Toast.LENGTH_SHORT).show(); return
            }
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Inventario escaneado")
                putExtra(Intent.EXTRA_TEXT, "Adjunto archivo de inventario escaneado.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Enviar inventario por correo"))
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_error_enviar, e.message), Toast.LENGTH_LONG).show()
        }
    }

    private fun exportarCSVAcumulado(uri: android.net.Uri) {
        viewModel.exportarInventario(uri) { success, message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            if (success) mostrarAcumuladoQE()
        }
    }

    private fun cargarCSVAcumulado(uri: android.net.Uri) {
        viewModel.importarInventario(uri) { success, message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            if (success) mostrarAcumuladoQE()
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("inventory_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("inventory_reset", false)) {
            resetearCampos()
            prefs.edit().putBoolean("inventory_reset", false).apply()
        }
    }

    private fun resetearCampos() {
        binding.etSkuManual.setText("")
        binding.etUbicacion.setText("")
        binding.etTotalPallets.setText("")
        binding.etSKU.setText("")
        binding.etDP.setText("")
        binding.etCxPal.setText("")
        binding.etFPC.setText("")
        binding.etCon.setText("")
        binding.etCentro.setText("")
        binding.etLINEA.setText("")
        binding.etOP.setText("")
        binding.etFProd.setText("")
        binding.etDiasV.setText("")
        limpiarCamposQRAdicionales()
        Toast.makeText(this, "Inventario reseteado a 0", Toast.LENGTH_SHORT).show()
        mostrarAcumuladoQE()
    }

    private fun limpiarCamposQRAdicionales() { /* Placeholder para futuros campos */
    }

    private fun resetearInventario() {
        viewModel.resetearInventario { exito, mensaje ->
            if (exito) {
                Toast.makeText(this, getString(R.string.toast_inventario_reseteado), Toast.LENGTH_SHORT).show()
                mostrarAcumuladoQE()
            } else {
                Toast.makeText(this, mensaje ?: "Error al resetear inventario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarAcumuladoQE() {
        val lista = viewModel.obtenerAcumuladoPorTarima()
        val builder = StringBuilder()
        var total = 0
        val totalesPorTipo = mutableMapOf<String, Int>()
        for ((sku, mapTarima) in lista.entries) {
            for ((tipo, cantidad) in mapTarima.entries) {
                builder.append("SKU: $sku, Pallets: $cantidad, Tarima: $tipo\n")
                total += cantidad
                totalesPorTipo[tipo] = (totalesPorTipo[tipo] ?: 0) + cantidad
            }
        }
        builder.append("\nTotales por tipo de tarima:\n")
        for ((tipo, suma) in totalesPorTipo.entries) {
            builder.append("Total Tarimas $tipo: $suma\n")
        }
        try {
            val tvAcumulado = binding.root.findViewById<TextView>(R.id.totalCajasTextView)
            val currentText = tvAcumulado.text.toString()
            tvAcumulado.text = "$currentText\n\nAcumulado por Tarima:\n$builder"
        } catch (e: Exception) {
            val currentText = binding.totalCajasTextView.text.toString()
            binding.totalCajasTextView.text = "$currentText\n\nAcumulado por Tarima:\n$builder"
        }
        Toast.makeText(this, "Acumulado: $total pallets", Toast.LENGTH_SHORT).show()
    }

    private fun escaneoPalletContinuo(qrText: String) {
        try {
            val datosLimpios = QrParser.parse(qrText)
            val sku = datosLimpios["SKU"] ?: ""
            val dp = datosLimpios["DP"] ?: ""
            val cxPal = datosLimpios["CxPal"] ?: ""
            val fpc = datosLimpios["FPC"] ?: ""
            val con = datosLimpios["Con"] ?: ""
            val centro = datosLimpios["Centro"] ?: ""
            val linea = datosLimpios["LINEA"] ?: ""
            val op = datosLimpios["OP"] ?: ""
            val fProd = datosLimpios["FProd"] ?: ""
            val diasV = datosLimpios["Dias V"] ?: ""
            binding.etSKU.setText(sku)
            binding.etDP.setText(dp)
            binding.etCxPal.setText(cxPal)
            binding.etFPC.setText(fpc)
            binding.etCon.setText(con)
            binding.etCentro.setText(centro)
            binding.etLINEA.setText(linea)
            binding.etOP.setText(op)
            binding.etFProd.setText(fProd)
            binding.etDiasV.setText(diasV)
            binding.etTotalPallets.setText("1")
            binding.totalCajasTextView.text = "Total cajas: ${cxPal}"
            val material = MaterialItem(
                sku = sku,
                descripcion = dp,
                cxPal = cxPal,
                fpc = fpc,
                con = con,
                centro = centro,
                linea = linea,
                op = op,
                fProd = fProd,
                diasV = diasV,
                ubicacion = binding.etUbicacion.text.toString(),
                totalPallets = "1",
                restos = "",
                tipoTarima = tipoTarimaSeleccionada ?: "KOF"
            )
            viewModel.guardarMaterialEnArchivo(material) { success, message ->
                runOnUiThread {
                    if (success) {
                        viewModel.registrarTarima(material.sku, 1)
                        mostrarAcumuladoQE()
                        Toast.makeText(this, "Pallet guardado y listo para siguiente escaneo", Toast.LENGTH_SHORT)
                            .show()
                        limpiarCampos()
                        mostrarSelectorTarima { seleccion ->
                            tipoTarimaSeleccionada = seleccion
                            prefs.edit().putString("ultima_tarima", seleccion).apply()
                            val options = ScanOptions().apply {
                                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                setPrompt(getString(R.string.scan_prompt))
                                setCameraId(0)
                                setBeepEnabled(true)
                                setBarcodeImageEnabled(true)
                            }
                            scanLauncher.launch(options)
                        }
                    } else {
                        Toast.makeText(this, "Error al guardar pallet: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar el código QR: ${e.message}", Toast.LENGTH_LONG).show()
            limpiarCampos()
        }
    }

    private fun escaneoRumba(qrData: String) {
        try {
            val datosLimpios = QrParser.parse(qrData)
            binding.etSKU.setText(datosLimpios["SKU"] ?: "")
            binding.etDP.setText(datosLimpios["DP"] ?: "")
            binding.etCxPal.setText(datosLimpios["CxPal"] ?: "")
            binding.etFPC.setText(datosLimpios["FPC"] ?: "")
            binding.etCon.setText(datosLimpios["Con"] ?: "")
            binding.etCentro.setText(datosLimpios["Centro"] ?: "")
            binding.etLINEA.setText(datosLimpios["LINEA"] ?: "")
            binding.etOP.setText(datosLimpios["OP"] ?: "")
            binding.etFProd.setText(datosLimpios["FProd"] ?: "")
            binding.etDiasV.setText(datosLimpios["Dias V"] ?: "")
            binding.etTotalPallets.setText("")
            tipoTarimaSeleccionada = tipoTarimaSeleccionada ?: "KOF"
            Toast.makeText(
                this,
                "Datos escaneados. Ingresa el total de pallets y presiona Guardar.",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar el código QR: ${e.message}", Toast.LENGTH_LONG).show()
            limpiarCampos()
        }
    }

    private fun limpiarCampos() {
        binding.etSKU.text.clear()
        binding.etDP.text.clear()
        binding.etCxPal.text.clear()
        binding.etFPC.text.clear()
        binding.etCon.text.clear()
        binding.etCentro.text.clear()
        binding.etLINEA.text.clear()
        binding.etOP.text.clear()
        binding.etFProd.text.clear()
        binding.etDiasV.text.clear()
        binding.etTotalPallets.setText("")
        binding.totalCajasTextView.text = "Total cajas: 0"
    }

    private var tipoTarimaSeleccionada: String? = null

    private fun crearMaterialDesdeUI(): MaterialItem = MaterialItem(
        sku = limpiarPrefijoCampo(binding.etSKU.text.toString(), "SKU"),
        descripcion = limpiarPrefijoCampo(binding.etDP.text.toString(), "DP"),
        cxPal = limpiarPrefijoCampo(binding.etCxPal.text.toString(), "CxPal"),
        fpc = limpiarPrefijoCampo(binding.etFPC.text.toString(), "FPC"),
        con = limpiarPrefijoCampo(binding.etCon.text.toString(), "Con"),
        centro = limpiarPrefijoCampo(binding.etCentro.text.toString(), "Centro"),
        linea = limpiarPrefijoCampo(binding.etLINEA.text.toString(), "LINEA"),
        op = limpiarPrefijoCampo(binding.etOP.text.toString(), "OP"),
        fProd = limpiarPrefijoCampo(binding.etFProd.text.toString(), "FProd"),
        diasV = limpiarPrefijoCampo(binding.etDiasV.text.toString(), "Dias V"),
        ubicacion = binding.etUbicacion.text.toString(),
        totalPallets = binding.etTotalPallets.text.toString(),
        restos = binding.etRestos.text.toString(),
        tipoTarima = tipoTarimaSeleccionada ?: "KOF",
        almacen = almacenSeleccionado?.nombreAlmacen
    )

    private fun validarCamposObligatorios(): Boolean {
        val tipoEscaneo = (intent.getStringExtra(EXTRA_TIPO_ESCANEO) ?: "").lowercase()
        val camposObligatorios = when (tipoEscaneo) {
            "rumba" -> listOf(
                binding.etSKU to "SKU",
                binding.etDP to "Descripción",
                binding.etTotalPallets to "Total Pallets"
            )

            "pallet" -> listOf(
                binding.etSKU to "SKU",
                binding.etDP to "Descripción"
            )

            "manual" -> listOf(
                binding.etSKU to "SKU",
                binding.etTotalPallets to "Total Pallets"
            )

            else -> listOf(
                binding.etSKU to "SKU",
                binding.etDP to "Descripción",
                binding.etCxPal to "CxPal"
            )
        }
        val campoVacio = camposObligatorios.firstOrNull { it.first.text.isNullOrBlank() }
        if (campoVacio != null) {
            val mensaje = "El campo '${campoVacio.second}' es obligatorio."
            Snackbar.make(findViewById(android.R.id.content), mensaje, Snackbar.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun mostrarDetallesComparacion(comparacion: List<com.example.escaneodematerialeskof.dashboard.ComparacionInventario>) {
        val scrollView = android.widget.ScrollView(this)
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(16, 16, 16, 16)
        scrollView.addView(layout)
        val titulo = TextView(this)
        titulo.text = "Detalles de la comparación"
        titulo.textSize = 18f
        titulo.setTypeface(null, android.graphics.Typeface.BOLD)
        layout.addView(titulo)
        val separador = View(this)
        separador.setBackgroundColor(android.graphics.Color.GRAY)
        val params = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            2
        )
        params.setMargins(0, 16, 0, 16)
        separador.layoutParams = params
        layout.addView(separador)
        val agrupados = comparacion.groupBy {
            when {
                it.estado.contains("Faltante") -> 0
                it.estado.contains("Sobrante") -> 1
                else -> 2
            }
        }.toSortedMap()
        for ((_, grupo) in agrupados) {
            for (item in grupo) {
                val itemView = TextView(this)
                val estado = when {
                    item.estado.contains("OK") -> "✅"
                    item.estado.contains("Faltante") -> "❌"
                    item.estado.contains("Sobrante") -> "🔄"
                    else -> "•"
                }
                val texto = StringBuilder()
                texto.append("$estado SKU: ${item.sku}\n")
                texto.append("   Descripción: ${item.descripcion}\n")
                texto.append("   Tipo tarima: ${item.tipoTarima}\n")
                texto.append("   Escaneado: ${item.escaneado ?: "N/A"}\n")
                texto.append("   Inventario: ${item.inventario ?: "N/A"}\n")
                texto.append("   Estado: ${item.estado}\n")
                itemView.text = texto.toString()
                itemView.setPadding(0, 8, 0, 8)
                layout.addView(itemView)
                val itemSeparador = View(this)
                itemSeparador.setBackgroundColor(android.graphics.Color.LTGRAY)
                val itemParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                itemParams.setMargins(0, 8, 0, 8)
                itemSeparador.layoutParams = itemParams
                layout.addView(itemSeparador)
            }
        }
        AlertDialog.Builder(this)
            .setTitle("Detalles de la comparación")
            .setView(scrollView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun guardarEscaneo(material: MaterialItem) {
        val prefs = getSharedPreferences("inventario_escaneos", MODE_PRIVATE)
        val gson = Gson()
        val listaJson = prefs.getString("lista_materiales", null)
        val type = object : TypeToken<MutableList<MaterialItem>>() {}.type
        val lista: MutableList<MaterialItem> =
            if (listaJson != null) gson.fromJson(listaJson, type) else mutableListOf()
        lista.add(material)
        prefs.edit().putString("lista_materiales", gson.toJson(lista)).apply()
    }

    private fun limpiarPrefijoCampo(valor: String?, campo: String): String {
        if (valor == null) return ""
        val prefijo = "$campo:"
        return if (valor.startsWith(prefijo)) valor.removePrefix(prefijo).trim() else valor.trim()
    }

    private lateinit var almacenManager: AlmacenCapacidadManager
    private var almacenSeleccionado: AlmacenCapacidad? = null

    private fun mostrarDialogoSeleccionAlmacen() {
        val almacenes = almacenManager.obtenerAlmacenes()
        if (almacenes.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Sin almacenes configurados")
                .setMessage("Debes configurar al menos un almacén antes de capturar inventario.")
                .setPositiveButton("OK") { _, _ -> finish() }
                .setCancelable(false)
                .show(); return
        }
        val nombres = almacenes.map { it.nombreAlmacen }.toTypedArray()
        var seleccionado = almacenSeleccionado?.let { almacenes.indexOf(it) }.takeIf { it != null && it >= 0 } ?: 0
        AlertDialog.Builder(this)
            .setTitle("Selecciona un almacén")
            .setSingleChoiceItems(nombres, seleccionado) { _, which ->
                seleccionado = which
            }
            .setPositiveButton("Aceptar") { dialog, _ ->
                almacenSeleccionado = almacenes[seleccionado]
                Toast.makeText(
                    this,
                    "Almacén seleccionado: ${almacenes[seleccionado].nombreAlmacen}",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .setCancelable(false)
            .show()
    }

    // --- Configuración de modos para mantener limpio onCreate ---
    private fun configurarModoRumba(tvTitulo: TextView) {
        tvTitulo.text = getString(R.string.captura_rumba)
        binding.btnEscanearGrande.visibility = View.VISIBLE
        binding.btnGuardar.visibility = View.VISIBLE
        binding.btnSiguiente.visibility = View.VISIBLE
        binding.btnExportar.visibility = View.VISIBLE
        binding.btnCargar.visibility = View.VISIBLE
        binding.etSkuManual.isEnabled = false
        binding.etUbicacion.isEnabled = true
        binding.etTotalPallets.isEnabled = true
        binding.etCxPal.visibility = View.GONE
        binding.etFPC.visibility = View.GONE
        binding.etCon.visibility = View.GONE
        binding.etCentro.visibility = View.GONE
        binding.etLINEA.visibility = View.GONE
        binding.etOP.visibility = View.GONE
        binding.etFProd.visibility = View.GONE
        binding.etDiasV.visibility = View.GONE
        binding.etRestos.visibility = View.GONE
        binding.btnEscanearGrande.text = getString(R.string.escanear_rumba)
        Toast.makeText(this, "Modo RUMBA: Escanee códigos QR de RUMBA", Toast.LENGTH_SHORT).show()
    }

    private fun configurarModoPallet(tvTitulo: TextView) {
        tvTitulo.text = getString(R.string.captura_pallets)
        binding.btnEscanearGrande.visibility = View.VISIBLE
        binding.btnGuardar.visibility = View.VISIBLE
        binding.btnSiguiente.visibility = View.VISIBLE
        binding.btnExportar.visibility = View.VISIBLE
        binding.btnCargar.visibility = View.VISIBLE
        binding.etSkuManual.isEnabled = false
        binding.etUbicacion.isEnabled = true
        binding.etTotalPallets.isEnabled = false
        binding.etTotalPallets.setText("1")
        binding.etCxPal.visibility = View.VISIBLE
        binding.etFPC.visibility = View.VISIBLE
        binding.etCon.visibility = View.VISIBLE
        binding.etCentro.visibility = View.VISIBLE
        binding.etLINEA.visibility = View.VISIBLE
        binding.etOP.visibility = View.VISIBLE
        binding.etFProd.visibility = View.VISIBLE
        binding.etDiasV.visibility = View.VISIBLE
        binding.etRestos.visibility = View.GONE
        binding.btnEscanearGrande.text = getString(R.string.escanear_pallets)
        Toast.makeText(this, getString(R.string.toast_escanear_pallet), Toast.LENGTH_SHORT).show()
    }

    private fun configurarModoManual(tvTitulo: TextView) {
        tvTitulo.text = getString(R.string.captura_manual)
        binding.btnEscanearGrande.visibility = View.VISIBLE
        binding.btnGuardar.visibility = View.VISIBLE
        binding.btnSiguiente.visibility = View.VISIBLE
        binding.btnExportar.visibility = View.VISIBLE
        binding.btnCargar.visibility = View.VISIBLE
        binding.etSKU.isEnabled = true
        binding.etTotalPallets.isEnabled = true
        binding.etDP.isEnabled = false
        binding.etFPC.isEnabled = false
        binding.etSKU.setText("")
        binding.etTotalPallets.setText("")
        binding.etCxPal.visibility = View.VISIBLE
        binding.etFPC.visibility = View.VISIBLE
        binding.etCon.visibility = View.VISIBLE
        binding.etCentro.visibility = View.VISIBLE
        binding.etLINEA.visibility = View.VISIBLE
        binding.etOP.visibility = View.VISIBLE
        binding.etFProd.visibility = View.VISIBLE
        binding.etDiasV.visibility = View.VISIBLE
        binding.etRestos.visibility = View.VISIBLE
        Toast.makeText(this, getString(R.string.toast_manual), Toast.LENGTH_SHORT).show()
    }

    private fun configurarModoDefault(tvTitulo: TextView) {
        tvTitulo.text = getString(R.string.inventory_capture)
        binding.btnEscanearGrande.visibility = View.VISIBLE
        binding.btnGuardar.visibility = View.VISIBLE
        binding.btnSiguiente.visibility = View.VISIBLE
        binding.btnExportar.visibility = View.VISIBLE
        binding.btnCargar.visibility = View.VISIBLE
        binding.etSkuManual.isEnabled = true
        binding.etUbicacion.isEnabled = true
        binding.etTotalPallets.isEnabled = true
        binding.etCxPal.visibility = View.VISIBLE
        binding.etFPC.visibility = View.VISIBLE
        binding.etCon.visibility = View.VISIBLE
        binding.etCentro.visibility = View.VISIBLE
        binding.etLINEA.visibility = View.VISIBLE
        binding.etOP.visibility = View.VISIBLE
        binding.etFProd.visibility = View.VISIBLE
        binding.etDiasV.visibility = View.VISIBLE
        binding.etRestos.visibility = View.VISIBLE
    }

    private fun mostrarSelectorTarima(onSelected: (String) -> Unit) {
        val tipos = arrayOf("KOF", "SAMS", "IEQSA", "CHEP")
        val idx = tipos.indexOf(tipoTarimaSeleccionada).takeIf { it >= 0 } ?: 0
        AlertDialog.Builder(this)
            .setTitle("Selecciona el tipo de tarima")
            .setSingleChoiceItems(tipos, idx) { _, which -> tipoTarimaTemp = tipos[which] }
            .setPositiveButton(getString(R.string.aceptar)) { d, _ ->
                d.dismiss()
                onSelected(tipoTarimaTemp ?: tipos[idx])
                tipoTarimaTemp = null
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private var tipoTarimaTemp: String? = null

    private fun tienePermisoCamara(): Boolean =
        checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun iniciarEscaneoQR() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt(getString(R.string.scan_prompt))
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
        }
        scanLauncher.launch(options)
    }
}
