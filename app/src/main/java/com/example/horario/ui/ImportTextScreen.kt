package com.example.horario.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.horario.data.ClassItem
import com.example.horario.ocr.DetectedClass
import com.example.horario.ocr.ScheduleParser

private val DAYS_T = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

private class TextRow(detected: DetectedClass) {
    var name by mutableStateOf(detected.name)
    var start by mutableStateOf(detected.startMinutes?.let { ClassItem.formatTime(it) } ?: "08:00")
    var end by mutableStateOf(
        detected.endMinutes?.let { ClassItem.formatTime(it) }
            ?: detected.startMinutes?.let { ClassItem.formatTime(it + 60) } ?: "09:00"
    )
    var room by mutableStateOf(detected.room)
    var day by mutableStateOf(0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTextScreen(
    onCancel: () -> Unit,
    onConfirm: (List<ClassItem>) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    val rows = remember { mutableStateListOf<TextRow>() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Pegar horario", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            "Copia tu horario (de la web de tu universidad o donde lo tengas) y pégalo aquí. " +
                "Detectaré materias, horas y aulas; tú eliges el día de cada una.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Pega aquí el texto del horario") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val detected = ScheduleParser.parse(text)
                rows.clear()
                rows.addAll(detected.map { TextRow(it) })
                status = if (detected.isEmpty())
                    "No detecté clases. Revisa que el texto incluya horas (ej. 12-14)."
                else
                    "Detecté ${detected.size}. Revisa, elige el día y guarda."
            },
            enabled = text.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Procesar texto") }

        Spacer(Modifier.height(8.dp))
        status?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(rows) { row -> TextRowCard(row) }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancelar") }
            Button(
                onClick = {
                    val items = rows.filter { it.day in 1..7 }.mapNotNull { r ->
                        val s = parseHHmmT(r.start) ?: return@mapNotNull null
                        val e = parseHHmmT(r.end) ?: return@mapNotNull null
                        if (e <= s) return@mapNotNull null
                        ClassItem(
                            name = r.name.trim().ifBlank { "Clase" },
                            room = r.room.trim(),
                            dayOfWeek = r.day,
                            startMinutes = s,
                            endMinutes = e,
                            reminderMinutes = 10
                        )
                    }
                    onConfirm(items)
                },
                enabled = rows.any { it.day in 1..7 },
                modifier = Modifier.weight(1f)
            ) { Text("Guardar (${rows.count { it.day in 1..7 }})") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextRowCard(row: TextRow) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = row.name,
                onValueChange = { row.name = it },
                label = { Text("Materia") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = row.start,
                    onValueChange = { row.start = it },
                    label = { Text("Inicio") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = row.end,
                    onValueChange = { row.end = it },
                    label = { Text("Fin") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = row.room,
                onValueChange = { row.room = it },
                label = { Text("Aula (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text("¿Qué día?", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items((1..7).toList()) { d ->
                    FilterChip(
                        selected = row.day == d,
                        onClick = { row.day = if (row.day == d) 0 else d },
                        label = { Text(DAYS_T[d - 1]) }
                    )
                }
            }
        }
    }
}

private fun parseHHmmT(text: String): Int? {
    val parts = text.trim().split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}
