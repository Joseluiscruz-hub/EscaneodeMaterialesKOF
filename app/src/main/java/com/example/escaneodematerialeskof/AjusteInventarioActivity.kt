package com.example.escaneodematerialeskof

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import com.example.escaneodematerialeskof.model.MaterialItem
import com.example.escaneodematerialeskof.ui.components.FloatingCalculatorBubble
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class AjusteInventarioActivity : AppCompatActivity() {

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++ // saltar comilla escapada
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
            i++
        }
        result.add(current.toString())
        return result
    }

    private var isAuthenticated = false
    private val CONTRASENA_GERENCIA = "Gerencia2025"

    override fun onCreate(savedInstanceState: Bundle?) {
        // Si tienes un tema personalizado, descomenta la siguiente línea y asegúrate de que exista en styles.xml
        // setTheme(R.style.Theme_EscaneodeMaterialesKOF)
        // Aplica el tema visual de Coca Cola definido en styles.xml
        setTheme(R.style.Theme_EscaneoDeMaterialesKOF)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajuste_inventario)

        // Solicitar contraseña al iniciar
        solicitarContrasena()
    }

    private fun solicitarContrasena() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Acceso Restringido - Gerencia")
        builder.setMessage("Ingrese la contraseña para acceder al ajuste de inventario:")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = "Contraseña"
        builder.setView(input)

        builder.setPositiveButton("Acceder") { _, _ ->
            val contrasenaIngresada = input.text.toString()
            if (contrasenaIngresada == CONTRASENA_GERENCIA) {
                isAuthenticated = true
                inicializarVista()
            } else {
                Toast.makeText(this, "Contraseña incorrecta. Acceso denegado.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        builder.setNegativeButton("Cancelar") { _, _ ->
            finish()
        }

        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
    }

    private fun inicializarVista() {
        if (!isAuthenticated) return

        val etBuscarSKU = findViewById<TextInputEditText>(R.id.etBuscarSKU)
        val btnBuscar = findViewById<Button>(R.id.btnBuscar)
        val cardDetalleProducto = findViewById<View>(R.id.cardDetalleProducto)
        val tvSKU = findViewById<TextView>(R.id.tvSKU)
        val tvDescripcion = findViewById<TextView>(R.id.tvDescripcion)
        val tvInventarioSistema = findViewById<TextView>(R.id.tvInventarioSistema)
        val tvInventarioEscaneado = findViewById<TextView>(R.id.tvInventarioEscaneado)
        val tvDiferencia = findViewById<TextView>(R.id.tvDiferencia)
        val etKOF = findViewById<EditText>(R.id.etKOF)
        val etCHEP = findViewById<EditText>(R.id.etCHEP)
        val etIEQSA = findViewById<EditText>(R.id.etIEQSA)
        val etSAMS = findViewById<EditText>(R.id.etSAMS)
        val etRestos = findViewById<EditText>(R.id.etRestos)
        val etComentario = findViewById<TextInputEditText>(R.id.etComentario)
        val btnGuardarAjuste = findViewById<Button>(R.id.btnGuardarAjuste)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)

        cardDetalleProducto.visibility = View.GONE

        btnBuscar.setOnClickListener {
            val sku = etBuscarSKU.text?.toString()?.trim()
            if (sku.isNullOrEmpty()) {
                Toast.makeText(this, "Ingresa un SKU para buscar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            buscarYMostrarProducto(
                sku, tvSKU, tvDescripcion, tvInventarioSistema,
                tvInventarioEscaneado, tvDiferencia, etKOF, etCHEP, etIEQSA, etSAMS,
                etRestos, etComentario, cardDetalleProducto
            )
        }

        // Agregar listeners para recalcular diferencias automáticamente
        val onTextChanged = object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                recalcularTotales(
                    tvInventarioEscaneado, tvDiferencia, tvInventarioSistema,
                    etKOF, etCHEP, etIEQSA, etSAMS, etRestos
                )
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etKOF.addTextChangedListener(onTextChanged)
        etCHEP.addTextChangedListener(onTextChanged)
        etIEQSA.addTextChangedListener(onTextChanged)
        etSAMS.addTextChangedListener(onTextChanged)
        etRestos.addTextChangedListener(onTextChanged)

        btnGuardarAjuste.setOnClickListener {
            guardarAjuste(
                etBuscarSKU, etKOF, etCHEP, etIEQSA, etSAMS, etRestos,
                etComentario, tvInventarioEscaneado
            )
        }

        btnCancelar.setOnClickListener {
            finish()
        }

        // Burbuja calculadora superpuesta
        val bubble = ComposeView(this).apply {
            setContent { MaterialTheme { FloatingCalculatorBubble() } }
        }
        addContentView(
            bubble,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun buscarYMostrarProducto(
        sku: String, tvSKU: TextView, tvDescripcion: TextView,
        tvInventarioSistema: TextView, tvInventarioEscaneado: TextView,
        tvDiferencia: TextView, etKOF: EditText, etCHEP: EditText,
        etIEQSA: EditText, etSAMS: EditText, etRestos: EditText,
        etComentario: TextInputEditText, cardDetalleProducto: View
    ) {
        val file = File(filesDir, "materiales_guardados.csv")
        if (!file.exists()) {
            Toast.makeText(this, "No hay materiales escaneados.", Toast.LENGTH_SHORT).show()
            return
        }

        val lines = file.readLines().drop(1) // Omitir encabezado
        val encontrados = lines.mapNotNull { line ->
            val cols = parseCsvLine(line)
            if (cols.isNotEmpty() && cols[0] == sku) {
                try {
                    MaterialItem(
                        sku = cols[0],
                        descripcion = cols.getOrNull(1) ?: "",
                        cxPal = cols.getOrNull(2) ?: "",
                        fpc = cols.getOrNull(3) ?: "",
                        con = cols.getOrNull(4) ?: "",
                        centro = cols.getOrNull(5) ?: "",
                        linea = cols.getOrNull(6) ?: "",
                        op = cols.getOrNull(7) ?: "",
                        fProd = cols.getOrNull(8) ?: "",
                        diasV = cols.getOrNull(9) ?: "",
                        ubicacion = cols.getOrNull(10) ?: "",
                        totalPallets = cols.getOrNull(11) ?: "",
                        restos = cols.getOrNull(17) ?: "" // Restos de ajuste si existiera columna extendida
                    )
                } catch (e: Exception) {
                    null
                }
            } else null
        }

        if (encontrados.isEmpty()) {
            Toast.makeText(this, "SKU no encontrado en materiales escaneados.", Toast.LENGTH_SHORT).show()
            cardDetalleProducto.visibility = View.GONE
            return
        }

        val material = encontrados.first()
        cardDetalleProducto.visibility = View.VISIBLE
        tvSKU.text = material.sku
        tvDescripcion.text = material.descripcion

        // Inventario sistema simulado (puedes conectar a datos reales si tienes)
        tvInventarioSistema.text = "0"

        // Mostrar valores actuales
        etKOF.setText(material.cxPal)
        etCHEP.setText("0")
        etIEQSA.setText("0")
        etSAMS.setText("0")
        etRestos.setText(material.restos ?: "0")
        etComentario.setText("")

        // Recalcular totales
        recalcularTotales(
            tvInventarioEscaneado, tvDiferencia, tvInventarioSistema,
            etKOF, etCHEP, etIEQSA, etSAMS, etRestos
        )
    }

    private fun recalcularTotales(
        tvInventarioEscaneado: TextView, tvDiferencia: TextView,
        tvInventarioSistema: TextView, etKOF: EditText, etCHEP: EditText,
        etIEQSA: EditText, etSAMS: EditText, etRestos: EditText
    ) {
        val kof = etKOF.text.toString().toIntOrNull() ?: 0
        val chep = etCHEP.text.toString().toIntOrNull() ?: 0
        val ieqsa = etIEQSA.text.toString().toIntOrNull() ?: 0
        val sams = etSAMS.text.toString().toIntOrNull() ?: 0

        val totalPallets = kof + chep + ieqsa + sams
        tvInventarioEscaneado.text = totalPallets.toString()

        val inventarioSistema = tvInventarioSistema.text.toString().toIntOrNull() ?: 0
        val diferencia = inventarioSistema - totalPallets
        tvDiferencia.text = diferencia.toString()

        // Cambiar color según la diferencia
        when {
            diferencia > 0 -> tvDiferencia.setTextColor(getColor(android.R.color.holo_orange_dark))
            diferencia < 0 -> tvDiferencia.setTextColor(getColor(android.R.color.holo_red_dark))
            else -> tvDiferencia.setTextColor(getColor(android.R.color.holo_green_dark))
        }
    }

    private fun guardarAjuste(
        etBuscarSKU: TextInputEditText, etKOF: EditText, etCHEP: EditText,
        etIEQSA: EditText, etSAMS: EditText, etRestos: EditText,
        etComentario: TextInputEditText, tvInventarioEscaneado: TextView
    ) {
        val sku = etBuscarSKU.text?.toString()?.trim()
        if (sku.isNullOrEmpty()) {
            Toast.makeText(this, "Ingresa un SKU para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(filesDir, "materiales_guardados.csv")
        if (!file.exists()) {
            Toast.makeText(this, "No hay materiales escaneados.", Toast.LENGTH_SHORT).show()
            return
        }

        val lines = file.readLines()
        val header = lines.firstOrNull() ?: ""
        val data = lines.drop(1).toMutableList()
        var found = false

        for (i in data.indices) {
            val cols = parseCsvLine(data[i]).toMutableList()
            // Asegurar que haya al menos 15 columnas para incluir todos los datos
            while (cols.size < 15) cols.add("")

            if (cols.isNotEmpty() && cols[0] == sku) {
                // Actualizar todos los campos editables
                cols[2] = etKOF.text?.toString() ?: "0" // cxPal (KOF)
                cols[11] = tvInventarioEscaneado.text?.toString() ?: "0" // totalPallets
                // No sobreescribir la columna 12 (TipoTarima). Los restos se agregan al comentario.

                // Agregar nuevos campos para CHEP, IEQSA, SAMS
                if (cols.size < 15) {
                    while (cols.size < 15) cols.add("0")
                }
                cols[13] = etCHEP.text?.toString() ?: "0" // CHEP
                cols[14] = etIEQSA.text?.toString() ?: "0" // IEQSA
                if (cols.size < 16) cols.add("0")
                cols[15] = etSAMS.text?.toString() ?: "0" // SAMS

                // Comentario (incluye restos si se ingresaron)
                if (cols.size < 17) cols.add("")
                val restosTxt = etRestos.text?.toString()?.trim().orEmpty()
                val comentarioTxt = etComentario.text?.toString()?.trim().orEmpty()
                cols[16] = if (restosTxt.isNotEmpty()) {
                    "Restos=" + restosTxt + "; " + comentarioTxt
                } else comentarioTxt

                data[i] = cols.joinToString(",")
                found = true
                break
            }
        }

        if (found) {
            // Actualizar header si es necesario
            val newHeader = if (header.contains("CHEP")) header else
                "$header,CHEP,IEQSA,SAMS,Comentario_Ajuste"

            file.writeText(newHeader + "\n" + data.joinToString("\n"))
            Toast.makeText(this, "Ajuste de pallets guardado correctamente", Toast.LENGTH_LONG).show()

            // Mostrar resumen del ajuste
            mostrarResumenAjuste(sku, etKOF, etCHEP, etIEQSA, etSAMS, etRestos, etComentario)
        } else {
            Toast.makeText(this, "SKU no encontrado para ajustar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarResumenAjuste(
        sku: String, etKOF: EditText, etCHEP: EditText, etIEQSA: EditText,
        etSAMS: EditText, etRestos: EditText, etComentario: TextInputEditText
    ) {
        val mensaje = """
            Ajuste realizado para SKU: $sku
            
            Pallets ajustados:
            • KOF: ${etKOF.text}
            • CHEP: ${etCHEP.text}
            • IEQSA: ${etIEQSA.text}
            • SAMS: ${etSAMS.text}
            • Restos: ${etRestos.text} cajas
            
            Total pallets: ${
            (etKOF.text.toString().toIntOrNull() ?: 0) +
                    (etCHEP.text.toString().toIntOrNull() ?: 0) +
                    (etIEQSA.text.toString().toIntOrNull() ?: 0) +
                    (etSAMS.text.toString().toIntOrNull() ?: 0)
        }
            
            Comentario: ${etComentario.text}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Ajuste Completado")
            .setMessage(mensaje)
            .setPositiveButton("Continuar") { _, _ ->
                // Limpiar formulario para nuevo ajuste
                limpiarFormulario()
            }
            .setNegativeButton("Salir") { _, _ -> finish() }
            .show()
    }

    private fun limpiarFormulario() {
        findViewById<TextInputEditText>(R.id.etBuscarSKU).setText("")
        findViewById<View>(R.id.cardDetalleProducto).visibility = View.GONE
        findViewById<EditText>(R.id.etKOF).setText("")
        findViewById<EditText>(R.id.etCHEP).setText("")
        findViewById<EditText>(R.id.etIEQSA).setText("")
        findViewById<EditText>(R.id.etSAMS).setText("")
        findViewById<EditText>(R.id.etRestos).setText("")
        findViewById<TextInputEditText>(R.id.etComentario).setText("")
    }
}
