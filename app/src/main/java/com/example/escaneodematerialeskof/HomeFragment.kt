package com.example.escaneodematerialeskof

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.example.escaneodematerialeskof.ui.home.util.TextAnimator
import com.example.escaneodematerialeskof.ui.importar.ImportarActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Cambiar a fragment_home_new
        return inflater.inflate(R.layout.fragment_home_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar animaciones y frases motivacionales
        setupAnimations(view)
        setupMotivationalPhrase(view)
        setupButtons(view) // Activar los botones principales
        setupDrawer(view) // Nueva función para la barra lateral
    }

    private fun setupDrawer(view: View) {
        val activity = requireActivity()
        val drawerLayout = activity.findViewById<androidx.drawerlayout.widget.DrawerLayout?>(R.id.drawerLayout)
        val navView = activity.findViewById<com.google.android.material.navigation.NavigationView?>(R.id.navView)
        // Listener para el menú lateral
        navView?.setNavigationItemSelectedListener { menuItem ->
            drawerLayout?.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_inicio -> {
                    // Ir a HomeFragment
                    // Ya estás en Home, puedes mostrar un mensaje o refrescar
                    Toast.makeText(requireContext(), "Pantalla de inicio", Toast.LENGTH_SHORT).show()
                }

                R.id.nav_capturar_inventario -> {
                    // Ir directo a escaneo de pallet automático
                    val intent = Intent(requireContext(), CapturaInventarioActivity::class.java)
                    intent.putExtra(CapturaInventarioActivity.EXTRA_TIPO_ESCANEO, "pallet")
                    startActivity(intent)
                }

                R.id.nav_dashboard, R.id.nav_comparar_inventarios -> {
                    val intent = Intent(requireContext(), InventoryComparisonActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_comparacion_tiempo_real -> {
                    // Ir a la pantalla de comparación en tiempo real
                    val intent = Intent(
                        requireContext(),
                        com.example.escaneodematerialeskof.ui.comparacion.ComparacionTiempoRealActivity::class.java
                    )
                    startActivity(intent)
                }

                R.id.nav_ajuste_inventario -> {
                    val intent = Intent(requireContext(), AjusteInventarioActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_configuracion -> {
                    mostrarDialogoContrasena()
                }

                R.id.nav_gestion_reinicio -> {
                    // Agregar acceso a la pantalla de gestión de reinicio corregida
                    val intent = Intent(
                        requireContext(),
                        com.example.escaneodematerialeskof.ui.gestion.GestionReinicioActivity::class.java
                    )
                    startActivity(intent)
                }

                R.id.nav_mas_opciones -> {
                    btnMasOpcionesClick()
                }
            }
            true
        }
    }

    private fun btnMasOpcionesClick() {
        val opciones = arrayOf(
            getString(R.string.configuracion),
            getString(R.string.borrar_inventarios),
            getString(R.string.resumen),
            getString(R.string.importar),
            getString(R.string.exportar)
        )
        MaterialAlertDialogBuilder(requireContext(), R.style.FemsaDialog)
            .setTitle(getString(R.string.opciones))
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> Toast.makeText(requireContext(), getString(R.string.configuracion), Toast.LENGTH_SHORT).show()
                    1 -> mostrarDialogoBorrarInventarios()
                    2 -> startActivity(Intent(requireContext(), NewInventarioResumenActivity::class.java))
                    3 -> startActivity(Intent(requireContext(), ImportarActivity::class.java))
                    4 -> {
                        val intent = Intent(requireContext(), CapturaInventarioActivity::class.java)
                        intent.putExtra("enviar_por_correo", true)
                        startActivity(intent)
                    }
                }
            }
            .show()
    }

    /**
     * Configura las animaciones del logo y fondo
     */
    private fun setupAnimations(view: View) {
        // Animar el logo con un efecto de entrada y pulso continuo
        val logoIntro = view.findViewById<LottieAnimationView>(R.id.logoIntro)
        logoIntro?.addAnimatorListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) {
                // Cuando termina la animación inicial, iniciar el pulso continuo
                TextAnimator.animatePulse(logoIntro)
            }
        })
        // Fondo animado opcional
        val particleBackground = view.findViewById<LottieAnimationView>(R.id.particleBackground)
        particleBackground?.playAnimation()
    }

    /**
     * Configura la frase motivacional con efecto de escritura
     */
    private fun setupMotivationalPhrase(view: View) {
        // Obtener una frase aleatoria y mostrarla con efecto de escritura
        val txtFrase = view.findViewById<TextView>(R.id.txtFrase)
        txtFrase?.text = com.example.escaneodematerialeskof.ui.home.util.FrasesMotivacionales.random()
    }

    /**
     * Configura los botones y sus animaciones
     */
    private fun setupButtons(view: View) {
        val btnCapturar = view.findViewById<Button>(R.id.btnCapturar)
        val btnDashboard = view.findViewById<Button>(R.id.btnDashboard)
        val btnComparacionTiempoReal = view.findViewById<Button>(R.id.btnComparacionTiempoReal)
        val btnConfiguracion = view.findViewById<Button>(R.id.btnConfiguracion)
        val btnMasOpciones = view.findViewById<Button>(R.id.btnMasOpciones)

        // Animar todos los botones con efecto de entrada
        TextAnimator.animarBotones(
            btnCapturar,
            btnDashboard,
            btnComparacionTiempoReal,
            btnConfiguracion,
            btnMasOpciones
        )

        // Configurar listeners para los botones
        btnCapturar.setOnClickListener {
            // Ir directo a escaneo de pallet automático
            val intent = Intent(requireContext(), CapturaInventarioActivity::class.java)
            intent.putExtra(CapturaInventarioActivity.EXTRA_TIPO_ESCANEO, "pallet")
            startActivity(intent)
        }

        btnDashboard.setOnClickListener {
            // Mostrar opciones de dashboard
            mostrarOpcionesDashboard()
        }

        btnComparacionTiempoReal.setOnClickListener {
            // Ir a la pantalla de comparación en tiempo real
            val intent = Intent(
                requireContext(),
                com.example.escaneodematerialeskof.ui.comparacion.ComparacionTiempoRealActivity::class.java
            )
            startActivity(intent)
        }

        btnConfiguracion.setOnClickListener {
            mostrarDialogoContrasena()
        }

        btnMasOpciones.setOnClickListener {
            btnMasOpcionesClick()
        }
    }

    private fun mostrarDialogoContrasena() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = getString(R.string.contrasena)
        val prefs = requireContext().getSharedPreferences("ajustes_inventario", android.content.Context.MODE_PRIVATE)
        val contrasenaGuardada = prefs.getString("contrasena", "Gerencia2025")

        MaterialAlertDialogBuilder(requireContext(), R.style.FemsaDialog)
            .setTitle(getString(R.string.ajustes_inventario))
            .setMessage(getString(R.string.confirmar_ajustes_inventario))
            .setView(input)
            .setPositiveButton(getString(R.string.aceptar)) { dialog, _ ->
                val contrasenaIngresada = input.text.toString()
                if (contrasenaIngresada == contrasenaGuardada) {
                    // Abrir la pantalla de ajustes de inventario
                    val intent = Intent(requireContext(), AjusteInventarioActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.contrasena_incorrecta), Toast.LENGTH_SHORT)
                        .show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .setNeutralButton(getString(R.string.cambiar_contrasena)) { _, _ ->
                mostrarDialogoCambiarContrasena()
            }
            .show()
    }

    private fun mostrarDialogoCambiarContrasena() {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        val inputActual = EditText(requireContext())
        inputActual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        inputActual.hint = getString(R.string.contrasena_actual)
        val inputNueva = EditText(requireContext())
        inputNueva.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        inputNueva.hint = getString(R.string.nueva_contrasena)
        layout.addView(inputActual)
        layout.addView(inputNueva)
        val prefs = requireContext().getSharedPreferences("ajustes_inventario", android.content.Context.MODE_PRIVATE)
        val contrasenaGuardada = prefs.getString("contrasena", "Gerencia2025")

        MaterialAlertDialogBuilder(requireContext(), R.style.FemsaDialog)
            .setTitle(getString(R.string.cambiar_contrasena))
            .setView(layout)
            .setPositiveButton(getString(R.string.guardar)) { dialog, _ ->
                val actual = inputActual.text.toString()
                val nueva = inputNueva.text.toString()
                if (actual == contrasenaGuardada) {
                    if (nueva.isNotBlank()) {
                        prefs.edit().putString("contrasena", nueva).apply()
                        Toast.makeText(requireContext(), getString(R.string.contrasena_actualizada), Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.contrasena_actual_incorrecta),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun mostrarDialogoBorrarInventarios() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = getString(R.string.contrasena)
        val prefs = requireContext().getSharedPreferences("ajustes_inventario", android.content.Context.MODE_PRIVATE)
        val contrasenaGuardada = prefs.getString("contrasena", "Gerencia2025")

        MaterialAlertDialogBuilder(requireContext(), R.style.FemsaDialog)
            .setTitle(getString(R.string.borrar_inventarios))
            .setMessage(getString(R.string.confirmar_borrar_inventarios))
            .setView(input)
            .setPositiveButton(getString(R.string.borrar)) { dialog, _ ->
                val contrasenaIngresada = input.text.toString()
                if (contrasenaIngresada == contrasenaGuardada) {
                    val prefsInv =
                        requireContext().getSharedPreferences("inventario", android.content.Context.MODE_PRIVATE)
                    prefsInv.edit().clear().apply()
                    Toast.makeText(requireContext(), getString(R.string.inventarios_borrados), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.contrasena_incorrecta), Toast.LENGTH_SHORT)
                        .show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun mostrarOpcionesDashboard() {
        val opciones = arrayOf(
            getString(R.string.comparar_inventarios),
            getString(R.string.ajustes_inventario),
            getString(R.string.resumen)
        )
        MaterialAlertDialogBuilder(requireContext(), R.style.FemsaDialog)
            .setTitle(getString(R.string.opciones_dashboard))
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> {
                        // Ir a la actividad de comparación de inventarios
                        val intent = Intent(requireContext(), InventoryComparisonActivity::class.java)
                        startActivity(intent)
                    }

                    1 -> {
                        // Mostrar diálogo de contraseña para ajustes de inventario
                        mostrarDialogoContrasena()
                    }

                    2 -> {
                        // Ir a la actividad de resumen de inventario
                        startActivity(Intent(requireContext(), NewInventarioResumenActivity::class.java))
                    }
                }
            }
            .show()
    }
}
