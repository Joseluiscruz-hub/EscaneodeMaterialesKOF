package com.example.escaneodematerialeskof.ui.opciones

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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import com.example.escaneodematerialeskof.R

/**
 * Activity for additional options and utilities.
 * Provides access to various tools and features not covered in the main screens.
 */
class MasOpcionesActivity : AppCompatActivity() {

    private lateinit var btnAcercaDe: Button
    private lateinit var btnAyuda: Button
    private lateinit var btnContacto: Button
    private lateinit var btnReportarError: Button
    private lateinit var btnConfiguracion: Button
    private lateinit var btnTema: Button
    private lateinit var btnIdioma: Button
    private lateinit var btnModoCaptura: Button

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
            text = "Más Opciones"
            textSize = 24f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        mainLayout.addView(titleText)
        
        // About Card
        val aboutCard = createCard()
        val aboutLayout = createCardLayout()
        
        val aboutTitle = createSectionTitle("Acerca de la Aplicación")
        aboutLayout.addView(aboutTitle)
        
        val aboutDesc = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Información sobre la aplicación, versión, y derechos de autor."
        }
        aboutLayout.addView(aboutDesc)
        
        btnAcercaDe = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Ver Información"
            setBackgroundColor(Color.parseColor("#2196F3")) // Material Blue
            setTextColor(Color.WHITE)
        }
        aboutLayout.addView(btnAcercaDe)
        
        aboutCard.addView(aboutLayout)
        mainLayout.addView(aboutCard)
        
        // Help Card
        val helpCard = createCard()
        val helpLayout = createCardLayout()
        
        val helpTitle = createSectionTitle("Ayuda y Soporte")
        helpLayout.addView(helpTitle)
        
        val helpDesc = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Guías de uso, tutoriales y preguntas frecuentes."
        }
        helpLayout.addView(helpDesc)
        
        btnAyuda = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Ver Ayuda"
            setBackgroundColor(Color.parseColor("#2196F3")) // Material Blue
            setTextColor(Color.WHITE)
        }
        helpLayout.addView(btnAyuda)
        
        helpCard.addView(helpLayout)
        mainLayout.addView(helpCard)
        
        // Contact Card
        val contactCard = createCard()
        val contactLayout = createCardLayout()
        
        val contactTitle = createSectionTitle("Contacto")
        contactLayout.addView(contactTitle)
        
        val contactDesc = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Información de contacto para soporte técnico y consultas."
        }
        contactLayout.addView(contactDesc)
        
        btnContacto = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Contactar Soporte"
            setBackgroundColor(Color.parseColor("#2196F3")) // Material Blue
            setTextColor(Color.WHITE)
        }
        contactLayout.addView(btnContacto)
        
        contactCard.addView(contactLayout)
        mainLayout.addView(contactCard)
        
        // Report Error Card
        val reportCard = createCard()
        val reportLayout = createCardLayout()
        
        val reportTitle = createSectionTitle("Reportar Error")
        reportLayout.addView(reportTitle)
        
        val reportDesc = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Reportar problemas o errores encontrados en la aplicación."
        }
        reportLayout.addView(reportDesc)
        
        btnReportarError = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Reportar Error"
            setBackgroundColor(Color.parseColor("#2196F3")) // Material Blue
            setTextColor(Color.WHITE)
        }
        reportLayout.addView(btnReportarError)
        
        reportCard.addView(reportLayout)
        mainLayout.addView(reportCard)
        
        // Botón de Configuración
        btnConfiguracion = Button(this).apply {
            text = "Configuración"
            setOnClickListener {
                mostrarDialogoConfiguracion()
            }
        }
        mainLayout.addView(btnConfiguracion)
        
        // Botones de Tema, Idioma y Modo de Captura
        btnTema = Button(this).apply {
            text = "Cambiar tema"
            setOnClickListener { mostrarDialogoTema() }
        }
        mainLayout.addView(btnTema)
        
        btnIdioma = Button(this).apply {
            text = "Cambiar idioma"
            setOnClickListener { mostrarDialogoIdioma() }
        }
        mainLayout.addView(btnIdioma)
        
        btnModoCaptura = Button(this).apply {
            text = "Modo de captura"
            setOnClickListener { mostrarDialogoModoCaptura() }
        }
        mainLayout.addView(btnModoCaptura)
        
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
        btnAcercaDe.setOnClickListener {
            showAcercaDeDialog()
        }
        
        btnAyuda.setOnClickListener {
            showAyudaDialog()
        }
        
        btnContacto.setOnClickListener {
            sendSupportEmail()
        }
        
        btnReportarError.setOnClickListener {
            reportError()
        }
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Acerca de KOF Inventario")
            .setMessage("""
                Aplicación para el control y gestión de Inventarios Coca Cola Femsa

                © 2025 KOF. Todos los derechos reservados.

                Desarrollado por Jose Luis Cruz / Jorge Mateos
                Operaciones Planta Cuautitlán
            """.trimIndent())
            .setPositiveButton("Aceptar", null)
            .show()
    }
    
    private fun showAcercaDeDialog() {
        val mensaje = "Escaneo de Materiales KOF\n\n" +
                "Versión: 1.0.0\n" +
                "Desarrollado por Jose Luis Cruz / Jorge Mateos\n" +
                "Operaciones Planta Cuautitlán\n" +
                "© 2025 Coca Cola FEMSA. Todos los derechos reservados.\n\n" +
                "Contacto: joseluiscruz0001@orstedcorp001.onmicrosoft.com"
        AlertDialog.Builder(this)
            .setTitle("Acerca de la aplicación")
            .setMessage(mensaje)
            .setPositiveButton("Cerrar", null)
            .show()
    }
    
    private fun sendSupportEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:soporte@kof.com")
            putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre aplicación de inventario")
        }
        
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No se encontró una aplicación de correo", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun reportError() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:errores@kof.com")
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de error en aplicación de inventario")
            putExtra(Intent.EXTRA_TEXT, "Descripción del error:\n\nPasos para reproducir:\n\nVersión de la aplicación: ${
                try {
                    packageManager.getPackageInfo(packageName, 0).versionName
                } catch (e: Exception) {
                    "1.0.0"
                }
            }")
        }
        
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No se encontró una aplicación de correo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoConfiguracion() {
        AlertDialog.Builder(this)
            .setTitle("Configuración")
            .setMessage("Aquí puedes agregar opciones de configuración personalizadas para la app.")
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun mostrarDialogoTema() {
        AlertDialog.Builder(this)
            .setTitle("Tema")
            .setItems(arrayOf("Claro", "Oscuro")) { _, which ->
                val tema = if (which == 0) "Claro" else "Oscuro"
                val prefs = getSharedPreferences("config", MODE_PRIVATE)
                prefs.edit().putString("tema", tema).apply()
                // Aplica el tema globalmente (requiere reiniciar actividad para efecto inmediato)
                Toast.makeText(this, "Tema seleccionado: $tema. Reinicia la app para aplicar cambios.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoIdioma() {
        AlertDialog.Builder(this)
            .setTitle("Idioma")
            .setItems(arrayOf("Español", "Inglés")) { _, which ->
                val idioma = if (which == 0) "es" else "en"
                val prefs = getSharedPreferences("config", MODE_PRIVATE)
                prefs.edit().putString("idioma", idioma).apply()
                Toast.makeText(this, "Idioma seleccionado: ${if (idioma == "es") "Español" else "Inglés"}. Reinicia la app para aplicar cambios.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoModoCaptura() {
        AlertDialog.Builder(this)
            .setTitle("Modo de captura")
            .setItems(arrayOf("Manual", "Automático")) { _, which ->
                val modo = if (which == 0) "Manual" else "Automático"
                val prefs = getSharedPreferences("config", MODE_PRIVATE)
                prefs.edit().putString("modo_captura", modo).apply()
                Toast.makeText(this, "Modo de captura: $modo", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAyudaDialog() {
        val mensaje = """
            📱 GUÍA DE USO - ESCANEO DE MATERIALES KOF

            ═══════════════════════════════════════

            🏠 PANTALLA PRINCIPAL
            • Accede a todas las funciones principales
            • Usa el menú lateral para navegación rápida

            ═══════════════════════════════════════

            📦 CAPTURA DE INVENTARIO

            Modos de escaneo:

            📸 MODO RUMBA:
            • Escanea el código QR del producto
            • Ingresa manualmente el total de pallets
            • Presiona "Guardar" para registrar

            🏷️ MODO PALLET:
            • Cada escaneo = 1 pallet automáticamente
            • Ideal para escaneo rápido continuo
            • Selecciona tipo de tarima antes de escanear

            ✏️ MODO MANUAL:
            • Ingresa datos sin escanear
            • Útil cuando el QR no funciona
            • Completa SKU y cantidad de pallets

            ═══════════════════════════════════════

            📊 RESUMEN DE INVENTARIO
            • Visualiza totales y gráficos
            • Filtra por almacén o SKU
            • Exporta a CSV para análisis
            • Comparte reportes por correo

            ═══════════════════════════════════════

            🔄 COMPARACIÓN TIEMPO REAL
            1. Carga inventario del sistema (CSV)
            2. Inicia escaneo de productos
            3. Observa diferencias en tiempo real
            4. Exporta resultados de comparación

            ═══════════════════════════════════════

            🏭 GESTIÓN DE ALMACENES
            • Crea y configura almacenes
            • Define capacidad máxima
            • Monitorea saturación en tiempo real
            • Edita o elimina almacenes existentes

            ═══════════════════════════════════════

            📥 IMPORTAR DATOS
            • Selecciona archivo CSV
            • Previsualiza antes de importar
            • Sincroniza con sistema central
            • Restaura respaldos anteriores

            ═══════════════════════════════════════

            ⚙️ CONFIGURACIÓN
            • Activa/desactiva notificaciones
            • Cambia entre modo claro/oscuro
            • Configura tiempo de escaneo
            • Limpia datos de la aplicación

            ═══════════════════════════════════════

            💡 TIPS Y CONSEJOS

            ✅ Mejores prácticas:
            • Selecciona el almacén antes de escanear
            • Usa modo PALLET para inventarios grandes
            • Exporta datos regularmente como respaldo
            • Revisa comparaciones para detectar errores

            ⚠️ Solución de problemas:
            • Si el QR no escanea: usa modo MANUAL
            • Si hay errores: verifica permisos de cámara
            • Si no exporta: verifica permisos de almacenamiento
            • Si falta algo: contacta soporte técnico

            ═══════════════════════════════════════

            📞 SOPORTE TÉCNICO
            Para asistencia adicional:
            • Email: joseluiscruz0001@orstedcorp001.onmicrosoft.com
            • Usa la opción "Reportar Error" en el menú
            • Incluye capturas de pantalla si es posible

            ═══════════════════════════════════════

            Versión 1.0.0 - Coca-Cola FEMSA
            © 2025 Todos los derechos reservados
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("📚 Ayuda y Guía de Uso")
            .setMessage(mensaje)
            .setPositiveButton("Entendido", null)
            .setNeutralButton("Contactar Soporte") { _, _ ->
                sendSupportEmail()
            }
            .show()
    }
}