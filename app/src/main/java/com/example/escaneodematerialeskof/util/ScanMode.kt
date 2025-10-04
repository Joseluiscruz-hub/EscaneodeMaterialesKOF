package com.example.escaneodematerialeskof.util

/**
 * Representa los modos de escaneo soportados en la app.
 */
sealed class ScanMode(val value: String) {
    object Rumba : ScanMode("rumba")
    object Pallet : ScanMode("pallet")
    object Manual : ScanMode("manual")
    object Default : ScanMode("")

    companion object {
        fun fromString(raw: String?): ScanMode = when (raw?.lowercase()) {
            Rumba.value -> Rumba
            Pallet.value -> Pallet
            Manual.value -> Manual
            else -> Default
        }
    }
}

