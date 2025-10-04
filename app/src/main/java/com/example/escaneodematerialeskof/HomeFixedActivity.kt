@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.escaneodematerialeskof

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.escaneodematerialeskof.ui.config.ConfiguracionActivity
import com.example.escaneodematerialeskof.ui.exportar.ExportarActivity
import com.example.escaneodematerialeskof.ui.importar.ImportarActivity
import com.example.escaneodematerialeskof.ui.opciones.MasOpcionesActivity
import com.example.escaneodematerialeskof.ui.theme.FemsaTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class HomeFixedActivity : AppCompatActivity() {

    companion object {
        private const val CODIGO_PERMISOS = 100
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permisos necesarios al iniciar
        solicitarPermisosNecesarios()

        setContent {
            FemsaTheme {
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            val items = listOf(
                                Triple("Inicio", Icons.Filled.Home) {
                                    /* solo cerrar */
                                },
                                Triple("Capturar inventario", Icons.Filled.CameraAlt) {
                                    showTipoEscaneoDialog()
                                },
                                Triple("Resumen de inventario", Icons.Filled.Assessment) {
                                    startActivity(
                                        Intent(
                                            this@HomeFixedActivity,
                                            NewInventarioResumenActivity::class.java
                                        )
                                    )
                                },
                                Triple("Comparar inventarios", Icons.AutoMirrored.Filled.CompareArrows) {
                                    startActivity(
                                        Intent(
                                            this@HomeFixedActivity,
                                            InventoryComparisonActivity::class.java
                                        )
                                    )
                                },
                                Triple("Ajuste de inventario", Icons.Filled.Build) {
                                    mostrarDialogoContrasena()
                                },
                                Triple("Importar", Icons.Filled.Upload) {
                                    startActivity(Intent(this@HomeFixedActivity, ImportarActivity::class.java))
                                },
                                Triple("Exportar", Icons.Filled.Download) {
                                    startActivity(Intent(this@HomeFixedActivity, ExportarActivity::class.java))
                                },
                                Triple("Configuración", Icons.Filled.Settings) {
                                    startActivity(Intent(this@HomeFixedActivity, ConfiguracionActivity::class.java))
                                },
                                Triple("Más opciones", Icons.Filled.MoreHoriz) {
                                    startActivity(Intent(this@HomeFixedActivity, MasOpcionesActivity::class.java))
                                }
                            )
                            val (selectedIndex, setSelectedIndex) = remember { mutableStateOf(0) }

                            items.forEachIndexed { index, (label, icon, action) ->
                                NavigationDrawerItem(
                                    label = { Text(label) },
                                    selected = index == selectedIndex,
                                    onClick = {
                                        setSelectedIndex(index)
                                        action.invoke()
                                        scope.launch { drawerState.close() }
                                    },
                                    icon = { Icon(icon, contentDescription = label) },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                            }
                        }
                    }
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("KOF Inventario") },
                                navigationIcon = {
                                    Text(
                                        text = "☰",
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .clickable { scope.launch { drawerState.open() } },
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            )
                        }
                    ) { paddingValues ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            HomeScreen(
                                onTipoEscaneo = {
                                    showTipoEscaneoDialog()
                                },
                                onCompararInventarios = {
                                    startActivity(Intent(this, InventoryComparisonActivity::class.java))
                                },
                                onAjustesInventario = {
                                    mostrarDialogoContrasena()
                                },
                                onMasOpciones = {
                                    startActivity(Intent(this, MasOpcionesActivity::class.java))
                                },
                                onCapturaInventario = {
                                    showTipoEscaneoDialog()
                                },
                                onComparar = {
                                    startActivity(Intent(this, InventoryComparisonActivity::class.java))
                                },
                                onResumen = {
                                    startActivity(Intent(this, NewInventarioResumenActivity::class.java))
                                },
                                onImportar = {
                                    startActivity(Intent(this, ImportarActivity::class.java))
                                },
                                onExportar = {
                                    startActivity(Intent(this, ExportarActivity::class.java))
                                },
                                onConfiguracion = {
                                    startActivity(Intent(this, ConfiguracionActivity::class.java))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun solicitarPermisosNecesarios() {
        val permisosRequeridos = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisosRequeridos.add(Manifest.permission.CAMERA)
        }

        // WRITE solo hasta 28, READ hasta 32
        if (Build.VERSION.SDK_INT <= 28 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permisosRequeridos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permisosRequeridos.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permisosRequeridos.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permisosRequeridos.toTypedArray(), CODIGO_PERMISOS)
        }
    }

    private fun showTipoEscaneoDialog() {
        val tipos = arrayOf("\uD83D\uDCE6 Rumba", "\uD83D\uDDF7️ Pallet", "\u270B Manual")
        MaterialAlertDialogBuilder(this, R.style.FemsaDialog)
            .setTitle("Seleccionar tipo de escaneo")
            .setItems(tipos) { _, which ->
                when (which) {
                    0 -> startCapturaInventario("rumba")
                    1 -> startCapturaInventario("pallet")
                    2 -> startCapturaInventario("manual")
                }
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun startCapturaInventario(tipo: String) {
        val intent = Intent(this, CapturaInventarioActivity::class.java)
        intent.putExtra(CapturaInventarioActivity.EXTRA_TIPO_ESCANEO, tipo)
        startActivity(intent)
    }

    private fun mostrarDialogoContrasena() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = getString(R.string.contrasena)
        }

        val prefs = getSharedPreferences("ajustes_inventario", MODE_PRIVATE)
        val contrasenaGuardada = prefs.getString("contrasena", null)

        MaterialAlertDialogBuilder(this, R.style.FemsaDialog)
            .setTitle(getString(R.string.ajustes_inventario))
            .setMessage(getString(R.string.confirmar_ajustes_inventario))
            .setView(input)
            .setPositiveButton(getString(R.string.aceptar)) { _, _ ->
                val contrasenaIngresada = input.text.toString()
                if (contrasenaGuardada != null && contrasenaIngresada == contrasenaGuardada) {
                    startActivity(Intent(this, AjusteInventarioActivity::class.java))
                } else {
                    val mensaje = if (contrasenaGuardada == null) {
                        "No hay contraseña configurada. Vaya a Configuración para establecer una."
                    } else {
                        getString(R.string.contrasena_incorrecta)
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODIGO_PERMISOS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permisos concedidos.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "La app requiere permisos para escanear y gestionar archivos.",
                    Toast.LENGTH_LONG
                ).show()
                // Según la lógica de negocio, se podría cerrar o limitar funciones
            }
        }
    }
}
