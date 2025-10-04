package com.example.escaneodematerialeskof.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
	primary = PrimaryDark,
	primaryContainer = PrimaryVariantDark,
	secondary = SecondaryDark,
	secondaryContainer = SecondaryVariantDark,
	background = BackgroundDark,
	surface = SurfaceDark,
	onPrimary = OnPrimaryDark,
	onSecondary = OnSecondaryDark,
	onBackground = OnBackgroundDark,
	onSurface = OnSurfaceDark,
	tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
	primary = Primary,
	primaryContainer = PrimaryVariant,
	secondary = Secondary,
	secondaryContainer = SecondaryVariant,
	background = Background,
	surface = Surface,
	onPrimary = OnPrimary,
	onSecondary = OnSecondary,
	onBackground = OnBackground,
	onSurface = OnSurface,
	tertiary = Pink40
)

@Composable
fun EscaneoDeMaterialesKOFTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	// Dynamic color is available on Android 12+
	dynamicColor: Boolean = true,
	content: @Composable () -> Unit
) {
	val colorScheme = when {
		dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
			val context = LocalContext.current
			if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
		}
		
		darkTheme -> DarkColorScheme
		else -> LightColorScheme
	}
	
	MaterialTheme(
		colorScheme = colorScheme,
		typography = Typography,
		content = content
	)
}