package de.uol.neuropsy.viewa.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary            = Blue40,
    onPrimary          = Color.White,
    primaryContainer   = Blue90,
    onPrimaryContainer = Blue10,
    secondary          = Teal40,
    onSecondary        = Color.White,
    secondaryContainer = Teal90,
    onSecondaryContainer = Blue10,
    tertiary           = Amber40,
    onTertiary         = Color.White,
    background         = Neutral99,
    onBackground       = Neutral10,
    surface            = Color.White,
    onSurface          = Neutral10,
    surfaceVariant     = Blue90,
    onSurfaceVariant   = Blue10,
)

private val DarkColorScheme = darkColorScheme(
    primary            = Blue80,
    onPrimary          = Blue20,
    primaryContainer   = Blue40,
    onPrimaryContainer = Blue90,
    secondary          = Teal80,
    onSecondary        = Blue10,
    secondaryContainer = Teal40,
    onSecondaryContainer = Teal90,
    tertiary           = Amber80,
    background         = Neutral10,
    onBackground       = Neutral90,
    surface            = Color(0xFF1C2B36),
    onSurface          = Neutral90,
)

@Composable
fun ViewaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic colour disabled: device wallpaper colours are unpredictable and
    // can produce very dark or low-contrast surfaces that break chart labels.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            // Use dark icons when the status-bar background is light, white icons when it's dark
            val useDarkIcons = android.graphics.Color.luminance(colorScheme.primary.toArgb()) > 0.5f
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = useDarkIcons
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}