package app.apartment.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light scheme only; the dark theme is not adopted (OI-044 d). Colors follow the roadmap mockups.
private val LightColors = lightColorScheme(
    primary = Color(0xFF0B6B5C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCEFE9),
    onPrimaryContainer = Color(0xFF00201A),
    secondaryContainer = Color(0xFFDCEFE9),
    onSecondaryContainer = Color(0xFF00201A),
    background = Color(0xFFF8FAF9),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A2420),
    onSurface = Color(0xFF1A2420),
    error = Color(0xFFB3261E),
)

@Composable
fun ApartmentTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
