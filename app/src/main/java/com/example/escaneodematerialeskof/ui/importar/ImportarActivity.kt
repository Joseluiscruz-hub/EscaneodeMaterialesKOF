package com.example.escaneodematerialeskof.ui.importar

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

/**
 * Activity for importing inventory data from CSV files.
 * Allows users to select and import different types of inventory data.
 */
class ImportarActivity : AppCompatActivity() {

    private lateinit var btnImportarInventario: Button
    private lateinit var btnImportarTarimas: Button
    private lateinit var btnImportarSistema: Button
    private lateinit var tvStatusImport: TextView

    // Register file picker launcher
    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { handleImportFile(it) }
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
            text = "Importar Datos"
            textSize = 24f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        mainLayout.addView(titleText)
        
        // Import Inventory Card
        val inventoryCard = createCard()
        val inventoryLayout = createCardLayout()
        
        val inventoryTitle = createSectionTitle("Importar Inventario")
        inventoryLayout.addView(inventoryTitle)
        
        val inventoryDesc = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Importa datos de inventario desde un archivo CSV. El formato debe ser: SKU,Descripción,Cantidad,FProd,DiasV"
        }
        inventoryLayout.addView(inventoryDesc)
        
        btnImportarInventario = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Seleccionar Archivo CSV"
            setBackgroundColor(Color.parseColor("#2196F3")) // Material Blue
            setTextColor(Color.WHITE)
        }
        inventoryLayout.addView(btnImportarInventario)
        
        inventoryCard.addView(inventoryLayout)
        mainLayout.addView(inventoryCard)
        
        // Import Tarimas Card
        val tarimasCard = createCard()
        val tarimasLayout = createCardLayout()
        
        val tarimasTitle = createSectionTitle("Importar Tarimas")
        tarimasLayout.addView(tarimasTitle)
        
        val tarimasDesc = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Importa datos de tarimas desde un archivo CSV. El formato debe ser: ID,SKU,Cantidad,Ubicación"
        }
        tarimasLayout.addView(tarimasDesc)
        
        btnImportarTarimas = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Seleccionar Archivo CSV"
            setBackgroundColor(Color.parseColor("#2196F3")) // Material Blue
            setTextColor(Color.WHITE)
        }
        tarimasLayout.addView(btnImportarTarimas)
        
        tarimasCard.addView(tarimasLayout)
        mainLayout.addView(tarimasCard)
        
        // Import Sistema Card
        val sistemaCard = createCard()
        val sistemaLayout = createCardLayout()
        
        val sistemaTitle = createSectionTitle("Importar Datos del Sistema")
        sistemaLayout.addView(sistemaTitle)
        
        val sistemaDesc = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Importa datos del sistema para comparación. El formato debe ser: SKU,Descripción,Cantidad"
        }
        sistemaLayout.addView(sistemaDesc)
        
        btnImportarSistema = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Seleccionar Archivo CSV"
            setBackgroundColor(Color.parseColor("#2196F3")) // Material Blue
            setTextColor(Color.WHITE)
        }
        sistemaLayout.addView(btnImportarSistema)
        
        sistemaCard.addView(sistemaLayout)
        mainLayout.addView(sistemaCard)
        
        // Status Text
        tvStatusImport = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 32
            }
            text = "Seleccione un archivo para importar"
            gravity = Gravity.CENTER
        }
        mainLayout.addView(tvStatusImport)
        
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
        btnImportarInventario.setOnClickListener {
            importLauncher.launch("text/csv")
        }
        
        btnImportarTarimas.setOnClickListener {
            importLauncher.launch("text/csv")
        }
        
        btnImportarSistema.setOnClickListener {
            importLauncher.launch("text/csv")
        }
    }
    
    private fun handleImportFile(uri: Uri) {
        tvStatusImport.text = "Importando archivo..."
        
        // Use the existing ViewModel to handle the import
        val viewModel = CapturaInventarioViewModel(application)
        viewModel.importarInventario(uri) { success, message ->
            tvStatusImport.text = message
            if (success) {
                Toast.makeText(this, "Importación exitosa", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error en la importación", Toast.LENGTH_SHORT).show()
            }
        }
    }
}