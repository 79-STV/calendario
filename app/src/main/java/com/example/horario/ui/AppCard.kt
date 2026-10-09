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

/** Estilo de tarjeta activo, accesible desde cualquier composable hijo. */
val LocalCardStyle = compositionLocalOf { CardStyle.NORMAL }

@Composable
fun ProvideCardStyle(style: CardStyle, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalCardStyle provides style, content = content)
}

/**
 * Tarjeta reutilizable. En modo NORMAL es una ElevatedCard estándar;
 * en modo GLASS usa un fondo translúcido con borde sutil (efecto "vidrio").
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    when (LocalCardStyle.current) {
        CardStyle.NORMAL -> {
            ElevatedCard(
                modifier = modifier,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) { content() }
        }
        CardStyle.GLASS -> {
            val base = MaterialTheme.colorScheme.surface
            val shape = RoundedCornerShape(18.dp)
            Box(
                modifier = modifier
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                base.copy(alpha = 0.55f),
                                base.copy(alpha = 0.30f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                        shape
                    )
            ) { content() }
        }
    }
}
