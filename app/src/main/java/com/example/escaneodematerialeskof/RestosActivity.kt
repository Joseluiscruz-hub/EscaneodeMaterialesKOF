package com.example.escaneodematerialeskof

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.escaneodematerialeskof.databinding.ActivityRestosBinding
import com.example.escaneodematerialeskof.manager.AlmacenCapacidadManager
import com.example.escaneodematerialeskof.model.MaterialItem
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class RestosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRestosBinding
    private val viewModel: CapturaInventarioViewModel by viewModels()
    private var acumulado: Int = 0

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result?.contents != null) {
            binding.etSKU.setText(result.contents)
            Toast.makeText(this, "Código QR leído", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        obtenerAcumuladoRestos()


        binding.btnEscanearCaja.setOnClickListener {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt("Escanea el código de la caja")
            options.setBeepEnabled(true)
            scanLauncher.launch(options)
        }

        binding.btnGuardarCaja.setOnClickListener {
            val sku = binding.etSKU.text.toString().trim()
            val cantidad = binding.etCantidad.text.toString().toIntOrNull() ?: 1
            if (sku.isEmpty()) {
                Toast.makeText(this, "Ingrese el SKU de la caja", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtener el almacén actual para asociar el registro como en el modo MANUAL
            val almacenManager = AlmacenCapacidadManager(this)
            val almacenActual = almacenManager.obtenerAlmacenActual() ?: ""

            // Crear MaterialItem para restos (comportamiento similar a modo MANUAL)
            val material = MaterialItem(
                sku = sku,
                descripcion = "",
                cxPal = "",
                fpc = "",
                con = "",
                centro = "",
                linea = "",
                op = "",
                fProd = "",
                diasV = "",
                ubicacion = "",
                totalPallets = cantidad.toString(),
                restos = "SI",
                tipoTarima = "RESTO",
                almacen = almacenActual
            )
            viewModel.guardarMaterialEnArchivo(material) { success, message ->
                runOnUiThread {
                    if (success) {
                        obtenerAcumuladoRestos()
                        binding.etSKU.setText("")
                        binding.etCantidad.setText("")
                        Toast.makeText(this, "Caja de restos guardada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        for (i in line.indices) {
            val c = line[i]
            when {
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                }

                c == '"' -> {
                    inQuotes = !inQuotes
                }

                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }

                else -> current.append(c)
            }
        }
        result.add(current.toString())
        return result
    }

    private fun obtenerAcumuladoRestos() {
        Thread {
            val file = java.io.File(filesDir, com.example.escaneodematerialeskof.util.Constants.INVENTORY_FILE_NAME)
            var total = 0
            if (file.exists()) {
                file.readLines().drop(1).forEach { line ->
                    val cols = parseCsvLine(line)
                    if (cols.size > 12 && cols[12] == "RESTO") {
                        total += cols.getOrNull(11)?.toIntOrNull() ?: 0
                    }
                }
            }
            runOnUiThread {
                acumulado = total
                actualizarAcumulado()
            }
        }.start()
    }

    private fun actualizarAcumulado() {
        binding.tvAcumuladoRestos.text = "Acumulado: $acumulado cajas"
    }
}
