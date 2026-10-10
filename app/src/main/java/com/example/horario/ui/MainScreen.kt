package com.example.horario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Grading
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private enum class Tab(val title: String) {
    HORARIO("Horario"),
    NOTAS("Notas")
}

// Paleta de colores de acento para elegir.
private val ACCENTS = listOf(
    0xFF7C4DFF, 0xFF2979FF, 0xFF00BFA5, 0xFF2E9E6B,
    0xFFE91E63, 0xFFF4511E, 0xFFFFB300, 0xFF8E24AA,
    0xFF00ACC1, 0xFF5E35B1, 0xFF43A047, 0xFFEC407A
).map { it.toInt() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    themeMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    accentColor: Int,
    onSelectAccent: (Int) -> Unit
) {
    var tab by remember { mutableStateOf(Tab.HORARIO) }
    var showPaste by remember { mutableStateOf(false) }
    var showAppearance by remember { mutableStateOf(false) }

    val scheduleVm: ScheduleViewModel = viewModel()
    val gradesVm: GradesViewModel = viewModel()

    val greeting = remember {
        val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (h) {
            in 5..11 -> "Buenos días"
            in 12..18 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (tab == Tab.HORARIO) "$greeting 👋" else tab.title,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (tab == Tab.HORARIO) {
                        IconButton(onClick = { showPaste = true }) {
                            Icon(Icons.Outlined.ContentPaste, contentDescription = "Pegar horario")
                        }
                    }
                    IconButton(onClick = { showAppearance = true }) {
                        Icon(Icons.Outlined.Palette, contentDescription = "Apariencia")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (tab) {
                Tab.HORARIO -> {
                    val classesByDay by scheduleVm.classesByDay.collectAsStateWithLifecycle()
                    ScheduleScreen(
                        classesByDay = classesByDay,
                        onSave = scheduleVm::save,
                        onDelete = scheduleVm::delete
                    )
                }
                Tab.NOTAS -> {
                    val subjects by gradesVm.subjects.collectAsStateWithLifecycle()
                    GradesScreen(
                        subjects = subjects,
                        onAddSubject = gradesVm::addSubject,
                        onDeleteSubject = gradesVm::deleteSubject,
                        onAddActivity = gradesVm::addActivity,
                        onUpdateActivity = gradesVm::updateActivity,
                        onDeleteActivity = gradesVm::deleteActivity,
                        onApplyTemplate = gradesVm::applyTemplate
                    )
                }
            }

            // Barra de navegación FLOTANTE tipo píldora (estilo de la referencia).
            FloatingNavBar(
                tab = tab,
                onSelect = { tab = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
            )

            if (showPaste) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ImportTextScreen(
                        onCancel = { showPaste = false },
                        onConfirm = { items -> scheduleVm.saveAll(items); showPaste = false }
                    )
                }
            }
        }
    }

    if (showAppearance) {
        AppearanceDialog(
            themeMode = themeMode,
            onSelectMode = onSelectMode,
            accentColor = accentColor,
            onSelectAccent = onSelectAccent,
            onDismiss = { showAppearance = false }
        )
    }
}

@Composable
private fun FloatingNavBar(
    tab: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    // Fondo oscuro tipo píldora, con los dos ítems.
    val barColor = Color(0xFF17171C)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(barColor)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavPill(
            selected = tab == Tab.HORARIO,
            icon = Icons.Outlined.CalendarMonth,
            label = "Horario",
            onClick = { onSelect(Tab.HORARIO) }
        )
        NavPill(
            selected = tab == Tab.NOTAS,
            icon = Icons.Outlined.Grading,
            label = "Notas",
            onClick = { onSelect(Tab.NOTAS) }
        )
    }
}

@Composable
private fun NavPill(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) accent else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.75f)
        )
        if (selected) {
            Spacer(Modifier.size(6.dp))
            Text(label, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceDialog(
    themeMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    accentColor: Int,
    onSelectAccent: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apariencia") },
        text = {
            Column {
                Text("Tema", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeChip("Sistema", themeMode == ThemeMode.SYSTEM) { onSelectMode(ThemeMode.SYSTEM) }
                    ModeChip("Claro", themeMode == ThemeMode.LIGHT) { onSelectMode(ThemeMode.LIGHT) }
                    ModeChip("Oscuro", themeMode == ThemeMode.DARK) { onSelectMode(ThemeMode.DARK) }
                    ModeChip("Negro", themeMode == ThemeMode.AMOLED) { onSelectMode(ThemeMode.AMOLED) }
                }

                Spacer(Modifier.height(16.dp))
                Text("Color de acento", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(110.dp)
                ) {
                    items(ACCENTS) { c ->
                        val color = Color(c)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (c == accentColor) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { onSelectAccent(c) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (c == accentColor) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Listo") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}
