package com.example.horario.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Paletas de color disponibles para que el usuario elija.
 * Cada una define el color principal en claro y en oscuro + un acento.
 */
enum class ThemePalette(
    val displayName: String,
    val primaryLight: Color,
    val primaryDark: Color,
    val accent: Color
) {
    MORADO("Morado", Color(0xFF7C4DFF), Color(0xFFB39DFF), Color(0xFF35D6A4)),
    AZUL("Azul", Color(0xFF2979FF), Color(0xFF82B1FF), Color(0xFF00E5FF)),
    VERDE("Verde", Color(0xFF2E9E6B), Color(0xFF5BD6A0), Color(0xFFB2FF59)),
    ROSA("Rosa", Color(0xFFE91E63), Color(0xFFFF8AB4), Color(0xFFFFC107)),
    NARANJA("Naranja", Color(0xFFF4511E), Color(0xFFFF9E7D), Color(0xFFFFD54F)),
    TURQUESA("Turquesa", Color(0xFF009688), Color(0xFF4DD0C4), Color(0xFF80D8FF)),
    INDIGO("Índigo", Color(0xFF3F51B5), Color(0xFF9FA8DA), Color(0xFFFF80AB)),
    GRAFITO("Grafito", Color(0xFF546E7A), Color(0xFFB0BEC5), Color(0xFF80CBC4));

    companion object {
        fun fromName(name: String?): ThemePalette =
            entries.firstOrNull { it.name == name } ?: MORADO
    }
}

@Composable
fun HorarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: ThemePalette = ThemePalette.MORADO,
    content: @Composable () -> Unit
) {
    val lightColors = lightColorScheme(
        primary = palette.primaryLight,
        onPrimary = Color.White,
        secondary = palette.accent,
        onSecondary = Color(0xFF08261D),
        background = Color(0xFFFBFBFE),
        surface = Color(0xFFFFFFFF),
        onBackground = Color(0xFF1A1A1F),
        onSurface = Color(0xFF1A1A1F),
        surfaceVariant = Color(0xFFEDEAF6)
    )
    val darkColors = darkColorScheme(
        primary = palette.primaryDark,
        onPrimary = Color(0xFF1A1033),
        secondary = palette.accent,
        onSecondary = Color(0xFF04150F),
        background = Color(0xFF17171C),
        surface = Color(0xFF1F1F26),
        onBackground = Color(0xFFECECF1),
        onSurface = Color(0xFFECECF1),
        surfaceVariant = Color(0xFF2A2A33)
    )

    MaterialTheme(
        colorScheme = if (darkTheme) darkColors else lightColors,
        typography = Typography(),
        content = content
    )
}
