package com.example.horario.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Marca: verde menta + morado
private val Purple = Color(0xFF7C4DFF)
private val PurpleDark = Color(0xFFB39DFF)
private val Mint = Color(0xFF35D6A4)
private val MintDark = Color(0xFF4EE0B5)

private val LightColors = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    secondary = Mint,
    onSecondary = Color(0xFF08261D),
    background = Color(0xFFFBFBFE),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1A1F),
    onSurface = Color(0xFF1A1A1F),
    surfaceVariant = Color(0xFFEDEAF6)
)

private val DarkColors = darkColorScheme(
    primary = PurpleDark,
    onPrimary = Color(0xFF1A1033),
    secondary = MintDark,
    onSecondary = Color(0xFF04150F),
    background = Color(0xFF17171C),
    surface = Color(0xFF1F1F26),
    onBackground = Color(0xFFECECF1),
    onSurface = Color(0xFFECECF1),
    surfaceVariant = Color(0xFF2A2A33)
)

@Composable
fun HorarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
