# Escaneo de Materiales KOF

Sistema de escaneo y gestión de inventario de materiales para KOF (Coca-Cola FEMSA).

## 📱 Descripción

Aplicación Android desarrollada en Kotlin que permite el escaneo, gestión y control de inventario de materiales mediante
códigos QR. Incluye funcionalidades de comparación en tiempo real, dashboards ejecutivos y exportación de datos.

## 🚀 Características Principales

- **Escaneo de Materiales**: Captura mediante códigos QR de pallets, rumbas y restos
- **Gestión de Inventario**: Control completo del inventario con validaciones en tiempo real
- **Comparación de Inventarios**: Análisis y comparación entre diferentes capturas
- **Dashboard Ejecutivo**: Visualización de métricas y estadísticas
- **Exportación de Datos**: Generación de reportes en formato CSV/Excel
- **Gestión de Almacenes**: Control de capacidad y organización por almacenes
- **Modo Premium**: Interfaz mejorada con animaciones y diseño moderno

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose + XML Layouts
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Base de Datos**: Room Database
- **Librerías**:
    - Material Design 3
    - Lottie (Animaciones)
    - ZXing (Escaneo QR)
    - Retrofit (API REST)
    - Coroutines (Programación asíncrona)

## 📋 Requisitos

- Android SDK 24 o superior
- Android Studio Hedgehog o superior
- JDK 17

## 🔧 Instalación

1. Clona este repositorio:

```bash
git clone https://github.com/Joseluiscruz-hub/EscaneodeMaterialesKOF.git
```

2. Abre el proyecto en Android Studio

3. Sincroniza las dependencias de Gradle

4. Ejecuta la aplicación en un emulador o dispositivo físico

## 📂 Estructura del Proyecto

```
app/src/main/java/com/example/escaneodematerialeskof/
├── data/           # Capa de datos (Database, DAOs)
├── domain/         # Lógica de negocio
├── model/          # Modelos de datos
├── ui/             # Interfaces de usuario
│   ├── captura/    # Módulo de captura de inventario
│   ├── comparacion/# Módulo de comparación
│   ├── dashboard/  # Dashboards y reportes
│   ├── home/       # Pantalla principal
│   └── theme/      # Temas y estilos
├── util/           # Utilidades y helpers
└── viewmodel/      # ViewModels
```

## 📱 Funcionalidades

### Captura de Inventario

- Escaneo de códigos QR
- Validación de materiales
- Registro de pallets, rumbas y restos
- Conteo automático

### Comparación de Inventarios

- Comparación en tiempo real
- Detección de diferencias
- Análisis de variaciones
- Generación de reportes

### Dashboard Ejecutivo

- Métricas de inventario
- Gráficos y estadísticas
- Análisis por almacén
- Histórico de movimientos

### Gestión de Almacenes

- Control de capacidad
- Organización por zonas
- Alertas de ocupación

## 👥 Autor

- **José Luis Cruz** - [Joseluiscruz-hub](https://github.com/Joseluiscruz-hub)

## 📄 Licencia

Este proyecto es propiedad de Coca-Cola FEMSA.

## 📝 Notas de Versión

Ver [CAMBIOS_REALIZADOS.md](CAMBIOS_REALIZADOS.md) para el historial de cambios.
Ver [MEJORAS_IMPLEMENTADAS.md](MEJORAS_IMPLEMENTADAS.md) para las mejoras realizadas.

---

**Desarrollado para Coca-Cola FEMSA - Sistema de Control de Inventario**

