# Escaneo de Materiales KOF

Proyecto Android (Kotlin + Java) para captura, comparación y gestión de inventarios.

## Requisitos

- Android Studio Ladybug+ (o equivalente con Gradle 9)
- JDK 17 o 21
- Android SDK 34

## Compilar y ejecutar (Windows / Gradle Wrapper)

```bat
cd C:\Users\XxGol\IdeaProjects\EscaneodeMaterialesKOF
C:\Users\XxGol\IdeaProjects\EscaneodeMaterialesKOF\gradlew.bat clean assembleDebug --no-daemon
C:\Users\XxGol\IdeaProjects\EscaneodeMaterialesKOF\gradlew.bat installDebug --no-daemon
```

## Actividad principal

- LAUNCHER: `MainDashboardPremiumActivity`

## Pantallas clave

- Comparación en tiempo real: `ui/comparacion/ComparacionTiempoRealActivity`
- Gestión de almacenes: `ui/almacenes/GestionAlmacenesActivity`
- Configuración: `ui/config/ConfiguracionActivity`
- Más opciones: `ui/opciones/MasOpcionesActivity`

## Notas de mantenimiento

- Se migró el manejo del botón Atrás a `OnBackPressedDispatcher`.
- Se unificaron Activities duplicadas; las clases antiguas fueron marcadas como `@Deprecated` y redirigen a las nuevas (
  pueden eliminarse físicamente si se desea).
- Se externalizaron textos y colores a `res/values/*`.

## Problemas comunes

- Si faltan drawables o recursos, sincroniza Gradle desde Android Studio (File > Sync Project with Gradle Files).
- Si el wrapper no descarga dependencias, verifica conexión y permisos de proxy.
