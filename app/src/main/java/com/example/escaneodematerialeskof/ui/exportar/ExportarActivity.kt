package com.example.escaneodematerialeskof.ui.exportar

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import com.example.escaneodematerialeskof.CapturaInventarioViewModel
import com.example.escaneodematerialeskof.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity for exporting inventory data to CSV files.
 * Allows users to export different types of inventory data.
 */
class ExportarActivity : AppCompatActivity() {

    private lateinit var btnExportarInventario: Button
    private lateinit var btnExportarComparacion: Button
    private lateinit var btnExportarHistorial: Button
    private lateinit var tvStatusExport: TextView

    // Register file creation launcher
    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { handleExportFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create UI programmatically
        createUI()
        
        // Set up button click listeners
        setupClickListeners()
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
            text = "Exportar Datos"
            textSize = 24f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        mainLayout.addView(titleText)
        
        // Export Inventory Card
        val inventoryCard = createCard()
        val inventoryLayout = createCardLayout()
        
        val inventoryTitle = createSectionTitle("Exportar Inventario")
        inventoryLayout.addView(inventoryTitle)
        
        val inventoryDesc = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Exporta los datos de inventario actual a un archivo CSV. Incluye SKU, descripción, cantidad, fecha de producción y días de vida."
        }
        inventoryLayout.addView(inventoryDesc)
        
        btnExportarInventario = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Exportar Inventario"
            setBackgroundColor(Color.parseColor("#2196F3")) // Material Blue
            setTextColor(Color.WHITE)
        }
        inventoryLayout.addView(btnExportarInventario)
        
        inventoryCard.addView(inventoryLayout)
        mainLayout.addView(inventoryCard)
        
        // Export Comparison Card
        val comparisonCard = createCard()
        val comparisonLayout = createCardLayout()
        
        val comparisonTitle = createSectionTitle("Exportar Comparación")
        comparisonLayout.addView(comparisonTitle)
        
        val comparisonDesc = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Exporta los resultados de la comparación entre el inventario escaneado y los datos del sistema."
        }
        comparisonLayout.addView(comparisonDesc)
        
        btnExportarComparacion = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Exportar Comparación"
            setBackgroundColor(Color.parseColor("#2196F3")) // Material Blue
            setTextColor(Color.WHITE)
        }
        comparisonLayout.addView(btnExportarComparacion)
        
        comparisonCard.addView(comparisonLayout)
        mainLayout.addView(comparisonCard)
        
        // Export History Card
        val historyCard = createCard()
        val historyLayout = createCardLayout()
        
        val historyTitle = createSectionTitle("Exportar Historial")
        historyLayout.addView(historyTitle)
        
        val historyDesc = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Exporta el historial de escaneos realizados, incluyendo fecha, hora y usuario."
        }
        historyLayout.addView(historyDesc)
        
        btnExportarHistorial = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Exportar Historial"
            setBackgroundColor(Color.parseColor("#2196F3")) // Material Blue
            setTextColor(Color.WHITE)
        }
        historyLayout.addView(btnExportarHistorial)
        
        historyCard.addView(historyLayout)
        mainLayout.addView(historyCard)
        
        // Status Text
        tvStatusExport = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 32
            }
            text = "Seleccione una opción para exportar"
            gravity = Gravity.CENTER
        }
        mainLayout.addView(tvStatusExport)
        
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
    
    private fun setupClickListeners() {
        btnExportarInventario.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            exportLauncher.launch("inventario_$timestamp.csv")
        }
        
        btnExportarComparacion.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            exportLauncher.launch("comparacion_$timestamp.csv")
        }
        
        btnExportarHistorial.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            exportLauncher.launch("historial_$timestamp.csv")
        }
    }
    
    private fun handleExportFile(uri: Uri) {
        tvStatusExport.text = "Exportando archivo..."
        
        // Use the existing ViewModel to handle the export
        val viewModel = CapturaInventarioViewModel(application)
        viewModel.exportarInventario(uri) { success, message ->
            tvStatusExport.text = message
            if (success) {
                Toast.makeText(this, "Exportación exitosa", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error en la exportación", Toast.LENGTH_SHORT).show()
            }
        }
    }
}