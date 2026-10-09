package com.example.horario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.horario.data.ClassItem
import java.util.Calendar

private val DAY_NAMES = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
private val DAY_FULL = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    classesByDay: Map<Int, List<ClassItem>>,
    onSave: (ClassItem) -> Unit,
    onDelete: (ClassItem) -> Unit
) {
    // Día actual 1..7 (Lunes..Domingo) sin usar java.time (compatible con API 24)
    val today = remember {
        when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 7
        }
    }
    var selectedDay by remember { mutableStateOf(today) }
    var editing by remember { mutableStateOf<ClassItem?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // Minuto actual del día, para saber la "próxima clase de hoy".
    val nowMinutes = remember {
        val c = Calendar.getInstance()
        c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            DaySelector(selectedDay, today) { selectedDay = it }

            // Banner creativo: si estás viendo HOY, muestra la próxima clase.
            if (selectedDay == today) {
                val todayClasses = classesByDay[today].orEmpty().sortedBy { it.startMinutes }
                val next = todayClasses.firstOrNull { it.endMinutes > nowMinutes }
                NextClassBanner(next)
            }

            val dayClasses = classesByDay[selectedDay].orEmpty()
            if (dayClasses.isEmpty()) {
                EmptyState(DAY_FULL[selectedDay - 1])
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dayClasses, key = { it.id }) { item ->
                        ClassCard(
                            item = item,
                            onEdit = {
                                editing = item
                                showDialog = true
                            },
                            onDelete = { onDelete(item) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editing = null
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar clase")
        }
    }

    if (showDialog) {
        ClassEditorDialog(
            existing = editing,
            defaultDay = selectedDay,
            onDismiss = { showDialog = false },
            onConfirm = {
                onSave(it)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DaySelector(selected: Int, today: Int, onSelect: (Int) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items((1..7).toList()) { day ->
            FilterChip(
                selected = selected == day,
                onClick = { onSelect(day) },
                label = {
                    // Marca el día de hoy con un puntito.
                    if (day == today) Text("• ${DAY_NAMES[day - 1]}") else Text(DAY_NAMES[day - 1])
                }
            )
        }
    }
}

@Composable
private fun NextClassBanner(next: ClassItem?) {
    val text = if (next != null) {
        buildString {
            append("Próxima: ")
            append(next.name)
            append(" · ${next.startText}")
            if (next.room.isNotBlank()) append(" · ${next.room}")
        }
    } else {
        "No quedan más clases por hoy 🎉"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ClassCard(item: ClassItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    val accent = runCatching { Color(android.graphics.Color.parseColor(item.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accent)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    buildString {
                        append("${item.startText} – ${item.endText}")
                        if (item.room.isNotBlank()) append("  ·  ${item.room}")
                    },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                if (item.reminderMinutes > 0) {
                    Text(
                        "🔔 ${item.reminderMinutes} min antes",
                        color = accent,
                        fontSize = 12.sp
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Borrar")
            }
        }
    }
}

@Composable
private fun EmptyState(dayName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.EventNote,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Sin clases el $dayName",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Text(
                "Toca + para agregar una",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 13.sp
            )
        }
    }
}
