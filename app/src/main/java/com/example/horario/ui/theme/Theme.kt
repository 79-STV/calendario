package com.example.horario.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils

/** Variante de oscuridad para construir los esquemas. */
enum class DarkFlavor { LIGHT, DARK, AMOLED }

/** Aclara u oscurece un color para derivar la versión del acento en modo oscuro. */
private fun Int.lighten(factor: Float): Color {
    val base = this
    val white = android.graphics.Color.WHITE
    return Color(ColorUtils.blendARGB(base, white, factor))
}

@Composable
fun HorarioTheme(
    flavor: DarkFlavor,
    accentArgb: Int,
    content: @Composable () -> Unit
) {
    val accent = Color(accentArgb)
    val accentOnDark = accentArgb.lighten(0.35f)

    val colors = when (flavor) {
        DarkFlavor.LIGHT -> lightColorScheme(
            primary = accent,
            onPrimary = Color.White,
            secondary = accent,
            background = Color(0xFFF4F3FB),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFECEAF6),
            onBackground = Color(0xFF1A1A1F),
            onSurface = Color(0xFF1A1A1F)
        )
        DarkFlavor.DARK -> darkColorScheme(
            primary = accentOnDark,
            onPrimary = Color(0xFF11121A),
            secondary = accentOnDark,
            background = Color(0xFF14141A),
            surface = Color(0xFF1E1E27),
            surfaceVariant = Color(0xFF2A2A35),
            onBackground = Color(0xFFECECF1),
            onSurface = Color(0xFFECECF1)
        )
        DarkFlavor.AMOLED -> darkColorScheme(
            primary = accentOnDark,
            onPrimary = Color(0xFF000000),
            secondary = accentOnDark,
            background = Color(0xFF000000),
            surface = Color(0xFF0C0C0F),
            surfaceVariant = Color(0xFF17171C),
            onBackground = Color(0xFFF2F2F5),
            onSurface = Color(0xFFF2F2F5)
        )
    }

    // Formas grandes y redondeadas (estilo de la referencia).
    val shapes = Shapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(28.dp)
    )

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = shapes,
        content = content
    )
}
