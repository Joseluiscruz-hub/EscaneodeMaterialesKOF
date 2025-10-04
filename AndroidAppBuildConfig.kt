// Este archivo no es necesario para la configuración estándar de una app Android. Toda la configuración debe estar en build.gradle.kts o app/build.gradle.kts. Puedes eliminar este archivo de tu proyecto.

android {
	namespace = "com.example.escaneodematerialeskof"
	compileSdk = 35
	defaultConfig {
		applicationId = "com.example.escaneodematerialeskof"
		minSdk = 24
		targetSdk = 35
		versionCode = 1
		versionName = "1.0"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		compose = true
		viewBinding = true
	}
	dependenciesInfo {
		includeInApk = true
		includeInBundle = true
	}
	buildToolsVersion = "30.0.3"
	ndkVersion = "29.0.13113456 rc1"
}