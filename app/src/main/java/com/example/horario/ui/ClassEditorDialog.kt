package com.example.horario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.horario.data.ClassItem

private val COLORS = listOf(
    "#7C4DFF", "#35D6A4", "#FF6E6E", "#FFB300",
    "#4FC3F7", "#FF8A65", "#BA68C8", "#90A4AE"
)
private val DIALOG_DAYS = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassEditorDialog(
    existing: ClassItem?,
    defaultDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (ClassItem) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var room by remember { mutableStateOf(existing?.room ?: "") }
    var day by remember { mutableStateOf(existing?.dayOfWeek ?: defaultDay) }
    var start by remember { mutableStateOf(existing?.startText ?: "08:00") }
    var end by remember { mutableStateOf(existing?.endText ?: "09:00") }
    var color by remember { mutableStateOf(existing?.colorHex ?: COLORS.first()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Nueva clase" else "Editar clase") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Materia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Aula / nota (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Día", fontWeight = FontWeight.Medium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items((1..7).toList()) { d ->
                        FilterChip(
                            selected = day == d,
                            onClick = { day = d },
                            label = { Text(DIALOG_DAYS[d - 1]) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = start,
                        onValueChange = { start = it },
                        label = { Text("Inicio (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = end,
                        onValueChange = { end = it },
                        label = { Text("Fin (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Color", fontWeight = FontWeight.Medium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(COLORS) { c ->
                        val parsed = Color(android.graphics.Color.parseColor(c))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parsed)
                                .border(
                                    width = if (color == c) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { color = c }
                        )
                    }
                }

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val s = parseTime(start)
                val e = parseTime(end)
                when {
                    name.isBlank() -> error = "Escribe el nombre de la materia"
                    s == null || e == null -> error = "Hora inválida (usa HH:mm)"
                    e <= s -> error = "La hora de fin debe ser mayor"
                    else -> onConfirm(
                        (existing ?: ClassItem(
                            name = "", dayOfWeek = day, startMinutes = 0, endMinutes = 0
                        )).copy(
                            name = name.trim(),
                            room = room.trim(),
                            dayOfWeek = day,
                            startMinutes = s,
                            endMinutes = e,
                            colorHex = color
                        )
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/** "08:30" -> 510 ; null si es inválido. */
private fun parseTime(text: String): Int? {
    val parts = text.trim().split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}
