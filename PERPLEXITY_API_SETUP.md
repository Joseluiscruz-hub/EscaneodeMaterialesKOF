# Configuración de la API de Perplexity

## 📋 Descripción

Este proyecto incluye integración con la API de Perplexity para proporcionar asistencia inteligente basada en IA para
consultas relacionadas con inventario y logística.

## 🔑 Obtener tu API Key

1. Ve a [Perplexity AI](https://www.perplexity.ai/)
2. Crea una cuenta o inicia sesión
3. Ve a la sección de API: https://www.perplexity.ai/settings/api
4. Genera una nueva API Key
5. **IMPORTANTE**: Guarda tu API key de forma segura, no la compartas públicamente

## ⚙️ Configuración en la App

### Opción 1: Configuración Directa (No Recomendada para Producción)

En `MainDashboardActivity.kt`, línea 152:

```kotlin
val token = "pplx-TU_API_KEY_AQUI" // Reemplaza con tu API key real
```

### Opción 2: Usar Variables de Entorno (Recomendado)

1. Crea un archivo `local.properties` en la raíz del proyecto (si no existe)
2. Agrega tu API key:

```properties
PERPLEXITY_API_KEY=pplx-tu-api-key-aqui
```

3. Modifica `build.gradle.kts` del módulo `app`:

```kotlin
android {
    // ...
    defaultConfig {
        // ...
        buildConfigField("String", "PERPLEXITY_API_KEY", "\"${project.findProperty("PERPLEXITY_API_KEY") ?: ""}\"")
    }
    buildFeatures {
        buildConfig = true
    }
}
```

4. Usa la API key en tu código:

```kotlin
val token = BuildConfig.PERPLEXITY_API_KEY
```

### Opción 3: Almacenamiento Seguro (Más Seguro)

Usa Android Keystore o cifrado para almacenar la API key de forma segura.

## 📡 Endpoint y Modelos

### Endpoint

```
https://api.perplexity.ai/chat/completions
```

### Request

```json
{
  "model": "llama-3.1-sonar-small-128k-online",
  "messages": [
    {
      "role": "system",
      "content": "Eres un asistente experto en gestión de inventarios y logística."
    },
    {
      "role": "user",
      "content": "Tu pregunta aquí"
    }
  ]
}
```

### Response

```json
{
  "id": "...",
  "model": "llama-3.1-sonar-small-128k-online",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "Respuesta de la IA"
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 10,
    "completion_tokens": 50,
    "total_tokens": 60
  }
}
```

## 🎯 Modelos Disponibles

- **llama-3.1-sonar-small-128k-online** (Recomendado para uso general)
- **llama-3.1-sonar-large-128k-online** (Más potente pero más costoso)
- **llama-3.1-sonar-huge-128k-online** (Máxima capacidad)

Puedes cambiar el modelo en `PerplexityRequest.kt`:

```kotlin
data class PerplexityRequest(
    @SerializedName("model")
    val model: String = "llama-3.1-sonar-large-128k-online", // Cambia aquí
    // ...
)
```

## 💰 Precios (Aproximados)

- **Sonar Small**: ~$0.0001 por 1000 tokens
- **Sonar Large**: ~$0.001 por 1000 tokens
- **Sonar Huge**: ~$0.005 por 1000 tokens

Consulta los precios actuales en la documentación oficial de Perplexity.

## 🧪 Probar la Integración

1. Configura tu API key siguiendo una de las opciones anteriores
2. Ejecuta la aplicación
3. En el Dashboard, presiona el botón "Test Perplexity"
4. Deberías ver un Toast con la respuesta de la IA

## 📝 Ejemplo de Uso

```kotlin
// En tu Activity o ViewModel
val perplexityViewModel = ViewModelProvider(this)[PerplexityViewModel::class.java]

// Observar la respuesta
perplexityViewModel.answer.observe(this) { response ->
    val answer = response.choices.firstOrNull()?.message?.content ?: "Sin respuesta"
    // Mostrar la respuesta al usuario
}

// Observar errores
perplexityViewModel.error.observe(this) { error ->
    // Manejar el error
}

// Hacer una consulta
perplexityViewModel.getAnswer(
    token = "pplx-tu-api-key",
    input = "¿Cuál es la mejor forma de organizar un almacén de bebidas?"
)
```

## ⚠️ Seguridad

### ❌ NO HAGAS ESTO:

```kotlin
// NO expongas tu API key en el código
val token = "pplx-1234567890abcdef" 
```

### ✅ HAZ ESTO:

```kotlin
// Usa BuildConfig o almacenamiento seguro
val token = BuildConfig.PERPLEXITY_API_KEY
```

### Antes de Subir a GitHub:

1. Asegúrate de que `local.properties` esté en `.gitignore`
2. No incluyas API keys en el código
3. Usa variables de entorno o secretos de GitHub Actions para CI/CD

## 🔧 Solución de Problemas

### Error 401 - Unauthorized

- Verifica que tu API key sea correcta
- Asegúrate de incluir el prefijo "Bearer " en el header de autorización

### Error 429 - Too Many Requests

- Has excedido el límite de rate limiting
- Espera un momento antes de hacer más solicitudes

### Error 500 - Server Error

- Error del servidor de Perplexity
- Intenta nuevamente más tarde

### No hay respuesta

- Verifica tu conexión a internet
- Asegúrate de que la estructura del request sea correcta
- Revisa los logs para más detalles

## 📚 Recursos Adicionales

- [Documentación Oficial de Perplexity API](https://docs.perplexity.ai/)
- [Ejemplos de uso](https://docs.perplexity.ai/examples)
- [Límites y Precios](https://docs.perplexity.ai/pricing)

## 🆘 Soporte

Si tienes problemas con la integración:

1. Verifica los logs de Android (Logcat)
2. Asegúrate de tener permisos de INTERNET en el AndroidManifest.xml
3. Verifica que Retrofit y Gson estén correctamente configurados
4. Consulta la documentación oficial de Perplexity

---

**Nota**: Esta integración es opcional. La aplicación funciona perfectamente sin la API de Perplexity.

