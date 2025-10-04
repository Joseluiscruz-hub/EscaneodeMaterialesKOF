import com.google.devtools.ksp.gradle.KspTaskJvm
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}

android {
    namespace = "com.example.escaneodematerialeskof"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.escaneodematerialeskof"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Cargar API keys desde apikeys.properties
        val apiKeysFile = rootProject.file("apikeys.properties")
        val apiKeys = java.util.Properties()
        if (apiKeysFile.exists()) {
            apiKeys.load(java.io.FileInputStream(apiKeysFile))
        }

        // Configurar BuildConfig con la API key de Perplexity
        buildConfigField("String", "PERPLEXITY_API_KEY", "\"${apiKeys.getProperty("PERPLEXITY_API_KEY", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = false // Desactivado explícitamente (no se usan layouts <layout>)
        buildConfig = true // Habilitar BuildConfig para API keys
    }
    dependenciesInfo {
        includeInApk = true
        includeInBundle = true
    }
    ndkVersion = "29.0.13113456 rc1"
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.preference.ktx)
    implementation("androidx.gridlayout:gridlayout:1.0.0") // Añadido para GridLayout

    // Jetpack Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.materialIconsExtended)

    // ConstraintLayout
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation(libs.bundles.navigation)

    // ZXing and JourneyApps barcode scanning
    implementation(libs.zxing.embedded)
    implementation(libs.zxing.core)

    // Room Database
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // MPAndroidChart
    implementation(libs.github.mpandroidchart)

    // Lottie for animations
    implementation(libs.airbnb.lottie)
    implementation(libs.airbnb.lottie.compose)

    // Gson for JSON parsing
    implementation(libs.google.gson)

    // Retrofit for network calls
    implementation(libs.bundles.retrofit)

    // SwipeRefreshLayout for pull-to-refresh support
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}


// Extra safety: disable any dataBinding tasks (should be unused as dataBinding=false)
tasks.configureEach {
    if (name.startsWith("dataBinding", ignoreCase = true)) {
        enabled = false
    }
}

// Workaround: avoid Gradle pre-execution cleanup for KSP tasks on Windows (file locks)
// Additionally, proactively clean KSP outputs with retries to avoid lingering locks.

// Best-effort pre-clean of all KSP outputs with retries to avoid Windows file locks
val preCleanKspOutputs = tasks.register("preCleanKspOutputs") {
    group = "build"
    description = "Deletes build/generated/ksp with retries to avoid Windows file locks"
    doLast {
        val dir = layout.buildDirectory.dir("generated/ksp").get().asFile
        if (dir.exists()) {
            var attempts = 0
            while (dir.exists() && attempts < 8) {
                val ok = dir.deleteRecursively()
                if (!ok) {
                    Thread.sleep(500)
                }
                attempts++
            }
        }
    }
}

tasks.withType(KspTaskJvm::class.java).configureEach {
    // Ensure pre-clean runs before any KSP task
    dependsOn(preCleanKspOutputs)
}

// Mitigación Windows: limpieza preventiva de la caché de compilación Kotlin con reintentos
val preCleanKotlinCompileCache by tasks.registering {
    group = "build"
    description = "Elimina app/build/kotlin/compile<Variant>Kotlin/cacheable con reintentos antes de compilar"
    doLast {
        val kotlinDir = layout.buildDirectory.dir("kotlin").get().asFile
        if (kotlinDir.exists()) {
            // Buscar carpetas cacheable dentro de compile* variantes
            val targets = kotlinDir.walkTopDown().maxDepth(3).filter { f ->
                f.isDirectory && f.name == "cacheable"
            }.toList()
            targets.forEach { dir ->
                var attempts = 0
                while (dir.exists() && attempts < 6) {
                    val ok = dir.deleteRecursively()
                    if (!ok) {
                        System.gc()
                        Thread.sleep(300)
                    }
                    attempts++
                }
            }
        }
    }
}

// Hacer que compileDebugKotlin y compileReleaseKotlin dependan de la limpieza preventiva
listOf("compileDebugKotlin", "compileReleaseKotlin").forEach { taskName ->
    tasks.matching { it.name == taskName }.configureEach {
        dependsOn(preCleanKotlinCompileCache)
    }
}
