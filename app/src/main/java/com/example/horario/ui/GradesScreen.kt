package com.example.horario.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Grading
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.horario.data.Activity
import com.example.horario.data.Subject
import com.example.horario.data.SubjectWithActivities

@Composable
fun GradesScreen(
    subjects: List<SubjectWithActivities>,
    onAddSubject: (String) -> Unit,
    onDeleteSubject: (Subject) -> Unit,
    onAddActivity: (subjectId: Long, name: String, grade: Double, weight: Double) -> Unit,
    onUpdateActivity: (Activity) -> Unit,
    onDeleteActivity: (Activity) -> Unit,
    onApplyTemplate: (subjectId: Long) -> Unit = {}
) {
    var showAddSubject by remember { mutableStateOf(false) }
    val expanded = remember { mutableStateMapOf<Long, Boolean>() }
    var editingActivity by remember { mutableStateOf<Activity?>(null) }
    var addActivityForSubject by remember { mutableStateOf<Long?>(null) }

    Box(Modifier.fillMaxSize()) {
        if (subjects.isEmpty()) {
            EmptyGrades()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subjects, key = { it.subject.id }) { sa ->
                    SubjectCard(
                        data = sa,
                        isExpanded = expanded[sa.subject.id] == true,
                        onToggle = {
                            expanded[sa.subject.id] = !(expanded[sa.subject.id] ?: false)
                        },
                        onDeleteSubject = { onDeleteSubject(sa.subject) },
                        onAddActivity = { addActivityForSubject = sa.subject.id },
                        onEditActivity = { editingActivity = it },
                        onDeleteActivity = onDeleteActivity,
                        onApplyTemplate = { onApplyTemplate(sa.subject.id) }
                    )
                }
            }
        }

        androidx.compose.material3.FloatingActionButton(
            onClick = { showAddSubject = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar materia")
        }
    }

    if (showAddSubject) {
        TextInputDialog(
            title = "Nueva materia",
            label = "Nombre de la materia",
            initial = "",
            onDismiss = { showAddSubject = false },
            onConfirm = {
                onAddSubject(it)
                showAddSubject = false
            }
        )
    }

    addActivityForSubject?.let { subjectId ->
        ActivityDialog(
            existing = null,
            onDismiss = { addActivityForSubject = null },
            onConfirm = { name, grade, weight ->
                onAddActivity(subjectId, name, grade, weight)
                addActivityForSubject = null
            }
        )
    }

    editingActivity?.let { act ->
        ActivityDialog(
            existing = act,
            onDismiss = { editingActivity = null },
            onConfirm = { name, grade, weight ->
                onUpdateActivity(act.copy(name = name, grade = grade, weightPercent = weight))
                editingActivity = null
            }
        )
    }
}

@Composable
private fun SubjectCard(
    data: SubjectWithActivities,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDeleteSubject: () -> Unit,
    onAddActivity: () -> Unit,
    onEditActivity: (Activity) -> Unit,
    onDeleteActivity: (Activity) -> Unit,
    onApplyTemplate: () -> Unit
) {
    val grade = data.projectedGrade
    val status = GradeStatus.of(grade)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        // Cabecera: nombre + nota con color + flecha
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    data.subject.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${data.activities.size} actividades · ${format1(data.totalWeight)}% registrado",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            GradeBadge(grade, status)
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(Modifier.padding(start = 16.dp, end = 8.dp, bottom = 12.dp)) {
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Si aún no hay actividades, ofrecemos la plantilla predefinida.
                if (data.activities.isEmpty()) {
                    TemplateSuggestion(onApplyTemplate = onApplyTemplate)
                }

                data.activities.forEach { act ->
                    ActivityRow(
                        activity = act,
                        onEdit = { onEditActivity(act) },
                        onDelete = { onDeleteActivity(act) }
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    TextButton(onClick = onAddActivity) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar actividad")
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDeleteSubject) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Borrar materia", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateSuggestion(onApplyTemplate: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .padding(14.dp)
    ) {
        Column {
            Text(
                "✨ Plantilla predefinida",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Taller 1 (15%) · Taller 2 (15%) · Quiz (15%) · Parcial (20%) · Final (35%)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.FilledTonalButton(onClick = onApplyTemplate) {
                Text("Usar plantilla")
            }
        }
    }
}

@Composable
private fun ActivityRow(activity: Activity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(vertical = 8.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(activity.name, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "Vale ${format1(activity.weightPercent)}%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
        val st = GradeStatus.of(activity.grade)
        Text(
            format2(activity.grade),
            color = st.color,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Borrar actividad",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun GradeBadge(grade: Double, status: GradeStatus) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(status.color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                format2(grade),
                color = status.color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(status.label, color = status.color, fontSize = 10.sp)
        }
    }
}

@Composable
private fun EmptyGrades() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.Grading,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Sin materias todavía",
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

// ---- Diálogos ----

@Composable
private fun TextInputDialog(
    title: String,
    label: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                singleLine = true
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(value) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ActivityDialog(
    existing: Activity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, grade: Double, weight: Double) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var gradeText by remember { mutableStateOf(existing?.grade?.let { format2(it) } ?: "") }
    var weightText by remember {
        mutableStateOf(existing?.weightPercent?.let { format1(it) } ?: "")
    }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Nueva actividad" else "Editar actividad") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej. Parcial 1)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = gradeText,
                    onValueChange = { gradeText = it },
                    label = { Text("Nota (1.0 – 5.0)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Porcentaje que vale (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val grade = gradeText.replace(",", ".").toDoubleOrNull()
                val weight = weightText.replace(",", ".").toDoubleOrNull()
                when {
                    name.isBlank() -> error = "Escribe el nombre"
                    grade == null || grade < 0.0 || grade > 5.0 -> error = "Nota entre 0.0 y 5.0"
                    weight == null || weight < 0.0 || weight > 100.0 -> error = "Porcentaje entre 0 y 100"
                    else -> onConfirm(name.trim(), grade, weight)
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun format1(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else "%.1f".format(v)
private fun format2(v: Double): String = "%.2f".format(v)
