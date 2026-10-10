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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.horario.data.ClassItem
import com.example.horario.ocr.DetectedClass
import com.example.horario.ocr.ScheduleParser

private val DAYS_T = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

private const val AI_PROMPT =
    "Organiza este horario de clases. Devuélveme SOLO una línea por clase, sin texto extra, " +
    "con este formato exacto: Materia | Día | HoraInicio-HoraFin | Aula. " +
    "Usa días completos (Lunes, Martes, ...). Ejemplo: " +
    "Cálculo | Lunes | 08:00-10:00 | Aula 202. " +
    "Si una clase se repite en varios días, pon una línea por cada día. " +
    "Quita cualquier dato que no sea materia, día, hora o aula. Aquí está mi horario:"

private class TextRow(detected: DetectedClass) {
    var name by mutableStateOf(detected.name)
    var start by mutableStateOf(detected.startMinutes?.let { ClassItem.formatTime(it) } ?: "08:00")
    var end by mutableStateOf(
        detected.endMinutes?.let { ClassItem.formatTime(it) }
            ?: detected.startMinutes?.let { ClassItem.formatTime(it + 60) } ?: "09:00"
    )
    var room by mutableStateOf(detected.room)
    // Día detectado automáticamente (0 si no se detectó).
    var day by mutableStateOf(detected.day)
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
        val context = LocalContext.current

        Text("Pegar horario", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            "Pega tu horario con una clase por línea. Lo ideal es que quede así, sin datos de más:\n\n" +
                "Materia  |  Día  |  Hora inicio - Hora fin  |  Aula\n\n" +
                "La app detecta el día, la hora y el aula solita.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
        )
        Spacer(Modifier.height(10.dp))

        // Prompt listo para que la persona lo use con SU IA (ChatGPT, Gemini, etc.)
        OutlinedButton(
            onClick = {
                val clip = context.getSystemService(android.content.ClipboardManager::class.java)
                clip?.setPrimaryClip(
                    android.content.ClipData.newPlainText("prompt", AI_PROMPT)
                )
                status = "Prompt copiado. Pégalo en tu IA junto con la foto/texto de tu horario, y luego pega aquí el resultado."
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Copiar prompt para mi IA") }

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Pega aquí el texto del horario") },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
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
                    "Detecté ${detected.size}. Revisa y guarda (el día ya viene detectado)."
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
                            endMinutes = e
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
