package com.example.escaneodematerialeskof.util

/**
 * Constants used throughout the application.
 */
object Constants {
    // File names
    const val INVENTORY_FILE_NAME = "materiales_guardados.csv"

    // CSV Headers (alineado con columnas: hasta TipoTarima y Almacen)
    const val CSV_HEADER = "SKU,DP,CxPal,FPC,Con,Centro,LINEA,OP,FProd,Dias V,Ubicacion,TotalPallets,TipoTarima,Almacen"

    // Shared Preferences
    const val PREFS_NAME = "inventory_prefs"
    const val PREF_INVENTORY_RESET = "inventory_reset"

    // Server URLs
    const val SERVER_URL = "http://your-server-url.com/api/inventory"

    // Request Codes
    const val REQUEST_CODE_SCAN = 1001
    const val REQUEST_CODE_EXPORT = 3003
    const val REQUEST_CODE_IMPORT = 2002

    // Otros
    const val CAPACIDAD_PALLETS_OBJETIVO = 5000 // Capacidad objetivo para calcular progreso
}