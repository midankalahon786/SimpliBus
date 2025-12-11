package com.example.simplibus.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val BusOrange = Color(0xFFD98E4C)
val BusCream = Color(0xFFF8F3E8)
val BusDarkGrey = Color(0xFF2C2C2C)
val BusLightGrey = Color(0xFFE7E7E7)

private val LightColorScheme = lightColorScheme(
    primary = BusOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBC0),
    onPrimaryContainer = Color(0xFF3E2400),

    secondary = BusDarkGrey,
    onSecondary = Color.White,
    secondaryContainer = BusLightGrey,
    onSecondaryContainer = Color(0xFF1F1F1F),

    tertiary = Color(0xFF5D5D5D),
    onTertiary = Color.White,

    background = BusCream,
    onBackground = BusDarkGrey,

    surface = Color.White,
    onSurface = BusDarkGrey,
    surfaceVariant = Color(0xFFEFE0CF),
    onSurfaceVariant = Color(0xFF4F4539),

    error = Color(0xFFBA1A1A),
    onError = Color.White
)
private val DarkColorScheme = darkColorScheme(
    primary = BusOrange,
    onPrimary = Color(0xFF3E2400),
    primaryContainer = Color(0xFF5B3D1B),
    onPrimaryContainer = Color(0xFFFFDBC0),

    secondary = Color(0xFFD3C4B4),
    onSecondary = Color(0xFF382E22),
    secondaryContainer = Color(0xFF4F4539),
    onSecondaryContainer = Color(0xFFEFE0CF),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun SimpliBusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}