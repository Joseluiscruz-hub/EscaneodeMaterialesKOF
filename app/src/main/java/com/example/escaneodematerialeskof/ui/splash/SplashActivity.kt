package com.example.escaneodematerialeskof.ui.splash

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.example.escaneodematerialeskof.R
import com.example.escaneodematerialeskof.ui.main.MainComposeActivity

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar orientación horizontal
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Configurar pantalla completa
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)

        // Deshabilitar el botón atrás durante el splash
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // No hacer nada - deshabilitar el botón atrás
            }
        })

        val videoView = findViewById<VideoView>(R.id.videoView)

        // Configurar la ruta del video (debe estar en res/raw/)
        val uri = Uri.parse("android.resource://$packageName/${R.raw.splash_video}")
        videoView.setVideoURI(uri)

        // Configurar listener para cuando termine el video
        videoView.setOnCompletionListener {
            // Ir a la actividad principal
            navigateToMainActivity()
        }

        // Iniciar reproducción del video
        videoView.start()

        // Backup: ir a la actividad principal después de 9 segundos por si hay problemas
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                navigateToMainActivity()
            }
        }, 9000)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainComposeActivity::class.java)
        startActivity(intent)
        finish() // Cerrar esta actividad para que no se pueda volver con el botón atrás
    }
}
