package com.example.escaneodematerialeskof.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper class para gestionar permisos de runtime en Android
 */
object PermissionHelper {

    const val REQUEST_CAMERA_PERMISSION = 100
    const val REQUEST_STORAGE_PERMISSION = 101
    const val REQUEST_ALL_PERMISSIONS = 102

    /**
     * Verifica si se tiene el permiso de cámara
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Verifica si se tienen los permisos de almacenamiento
     */
    fun hasStoragePermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ no requiere permisos de almacenamiento para archivos propios
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Solicita el permiso de cámara
     */
    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    /**
     * Solicita los permisos de almacenamiento
     */
    fun requestStoragePermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // En Android 13+ no se necesitan estos permisos
            return
        }

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_STORAGE_PERMISSION
        )
    }

    /**
     * Solicita todos los permisos necesarios para la app
     */
    fun requestAllPermissions(activity: Activity) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA)
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        ActivityCompat.requestPermissions(
            activity,
            permissions,
            REQUEST_ALL_PERMISSIONS
        )
    }

    /**
     * Verifica si se deben mostrar explicaciones de permisos
     */
    fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * Maneja el resultado de solicitud de permisos
     */
    fun handlePermissionResult(
        requestCode: Int,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION ||
            requestCode == REQUEST_STORAGE_PERMISSION ||
            requestCode == REQUEST_ALL_PERMISSIONS
        ) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onGranted()
            } else {
                onDenied()
            }
        }
    }
}
