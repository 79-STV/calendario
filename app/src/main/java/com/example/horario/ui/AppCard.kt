package com.example.horario.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/** Estilo de tarjeta y forma activos, accesibles desde cualquier composable hijo. */
val LocalCardStyle = compositionLocalOf { CardStyle.NORMAL }
val LocalCorner = compositionLocalOf { CornerShape.ROUND }
// Nombre propio para no chocar con androidx.compose.ui.platform.LocalDensity.
val LocalAppDensity = compositionLocalOf { Density.COMFY }

@Composable
fun ProvideAppStyle(
    cardStyle: CardStyle,
    corner: CornerShape,
    density: Density,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCardStyle provides cardStyle,
        LocalCorner provides corner,
        LocalAppDensity provides density,
        content = content
    )
}

/** Radio de esquina actual, como shape reutilizable. */
@Composable
fun appShape() = RoundedCornerShape(LocalCorner.current.radius.dp)

/**
 * Tarjeta reutilizable que respeta el estilo elegido:
 * NORMAL (elevada), GLASS (vidrio), FLAT (plano con borde fino),
 * OUTLINE (contorno grueso de acento), NEON (borde brillante de acento).
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = appShape()
    val style = LocalCardStyle.current
    val surface = MaterialTheme.colorScheme.surface
    val accent = MaterialTheme.colorScheme.primary

    when (style) {
        CardStyle.NORMAL -> {
            ElevatedCard(
                modifier = modifier,
                shape = shape,
                colors = CardDefaults.elevatedCardColors(containerColor = surface)
            ) { content() }
        }
        CardStyle.GLASS -> {
            Box(
                modifier = modifier
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(surface.copy(alpha = 0.55f), surface.copy(alpha = 0.30f))
                        )
                    )
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                        shape
                    )
            ) { content() }
        }
        CardStyle.FLAT -> {
            Box(
                modifier = modifier
                    .clip(shape)
                    .background(surface)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
                        shape
                    )
            ) { content() }
        }
        CardStyle.OUTLINE -> {
            Box(
                modifier = modifier
                    .clip(shape)
                    .background(surface)
                    .border(BorderStroke(2.5.dp, accent), shape)
            ) { content() }
        }
        CardStyle.NEON -> {
            Box(
                modifier = modifier
                    .clip(shape)
                    // Doble borde para simular brillo neón.
                    .border(BorderStroke(3.dp, accent.copy(alpha = 0.35f)), shape)
                    .clip(shape)
                    .background(surface)
                    .border(BorderStroke(1.5.dp, accent), shape)
            ) { content() }
        }
    }
}
