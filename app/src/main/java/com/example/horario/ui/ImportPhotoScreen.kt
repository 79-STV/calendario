package com.example.horario.ui

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.horario.data.ClassItem
import com.example.horario.ocr.DetectedClass
import com.example.horario.ocr.ScheduleParser
import com.example.horario.ocr.TextRecognizerRunner
import kotlinx.coroutines.launch
import java.io.File

private val DAYS = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

/** Fila editable de una clase detectada, con el día a elegir por el usuario. */
private class EditableRow(
    detected: DetectedClass
) {
    var name by mutableStateOf(detected.name)
    var start by mutableStateOf(detected.startMinutes?.let { ClassItem.formatTime(it) } ?: "08:00")
    var end by mutableStateOf(
        detected.endMinutes?.let { ClassItem.formatTime(it) }
            ?: detected.startMinutes?.let { ClassItem.formatTime(it + 60) } ?: "09:00"
    )
    var room by mutableStateOf(detected.room)
    var day by mutableStateOf(0) // 0 = sin elegir; 1..7
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPhotoScreen(
    onCancel: () -> Unit,
    onConfirm: (List<ClassItem>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val rows = remember { mutableStateListOf<EditableRow>() }

    // Uri temporal para la foto de cámara.
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    fun runOcr(uri: Uri) {
        loading = true
        status = null
        scope.launch {
            try {
                val text = TextRecognizerRunner.recognize(context, uri)
                val detected = ScheduleParser.parse(text)
                rows.clear()
                rows.addAll(detected.map { EditableRow(it) })
                status = if (detected.isEmpty())
                    "No detecté clases claras. Prueba una foto más nítida o agrégalas a mano."
                else
                    "Detecté ${detected.size}. Revisa, elige el día y guarda."
            } catch (e: Exception) {
                status = "No pude leer la imagen: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) runOcr(uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) cameraUri?.let { runOcr(it) } }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            status = "Sin permiso de cámara. Usa la galería."
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Importar desde foto", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            "Toma o elige una foto clara y derecha de tu horario. Detectaré materias, horas y aulas; tú eliges el día de cada una.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    cameraPermLauncher.launch(android.Manifest.permission.CAMERA)
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text("  Cámara")
            }
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Text("  Galería")
            }
        }

        Spacer(Modifier.height(12.dp))
        if (loading) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        status?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(rows) { row ->
                DetectedRowCard(row)
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    val items = rows
                        .filter { it.day in 1..7 }
                        .mapNotNull { r ->
                            val s = parseHHmm(r.start) ?: return@mapNotNull null
                            val e = parseHHmm(r.end) ?: return@mapNotNull null
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
            ) {
                Text("Guardar (${rows.count { it.day in 1..7 }})")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetectedRowCard(row: EditableRow) {
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
                        label = { Text(DAYS[d - 1]) }
                    )
                }
            }
        }
    }
}

private fun parseHHmm(text: String): Int? {
    val parts = text.trim().split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}

/** Crea un Uri temporal en caché para que la cámara guarde la foto. */
private fun createImageUri(context: Context): Uri {
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(dir, "horario_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
