package com.example.escaneodematerialeskof

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Main activity that serves as the entry point.
 * Its main job is to request permissions and then launch the main dashboard.
 */
class MainActivity : AppCompatActivity() {
    companion object {
        private const val CODIGO_PERMISOS = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ya no es necesario inflar el layout del dashboard aquí
        // Podríamos usar un layout de splash screen o dejarlo en blanco

        solicitarPermisosNecesarios()
    }

    private fun solicitarPermisosNecesarios() {
        val permisosRequeridos = mutableListOf<String>()

        // Verificar permiso de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisosRequeridos.add(Manifest.permission.CAMERA)
        }

        // Verificar permisos de almacenamiento
        if (android.os.Build.VERSION.SDK_INT <= 28) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permisosRequeridos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permisosRequeridos.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // Si ya tenemos los permisos, lanzamos el dashboard. Si no, los solicitamos
        if (permisosRequeridos.isEmpty()) {
            lanzarDashboard()
        } else {
            ActivityCompat.requestPermissions(this, permisosRequeridos.toTypedArray(), CODIGO_PERMISOS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODIGO_PERMISOS) {
            // Verificar si todos los permisos fueron concedidos
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Todos los permisos concedidos
                Toast.makeText(this, "Permisos concedidos.", Toast.LENGTH_SHORT).show()
                // Una vez concedidos los permisos, lanzamos el dashboard
                lanzarDashboard()
            } else {
                // Algunos permisos fueron denegados
                Toast.makeText(
                    this,
                    "La aplicación requiere todos los permisos para funcionar correctamente.",
                    Toast.LENGTH_LONG
                ).show()
                // Opcional: Puedes cerrar la app si los permisos son cruciales
                finish()
            }
        }
    }

    /**
     * Inicia la MainDashboardActivity y finaliza la MainActivity.
     */
    private fun lanzarDashboard() {
        val intent = Intent(this, MainDashboardActivity::class.java)
        startActivity(intent)
        // Finalizamos MainActivity para que el usuario no pueda volver a ella con el botón de retroceso
        finish()
    }
}
