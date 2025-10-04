package com.example.escaneodematerialeskof

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

@Deprecated("Usar ui.almacenes.GestionAlmacenesActivity")
class GestionAlmacenesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirigir inmediatamente a la Activity unificada
        startActivity(
            Intent(
                this,
                com.example.escaneodematerialeskof.ui.almacenes.GestionAlmacenesActivity::class.java
            )
        )
        finish()
    }
}
