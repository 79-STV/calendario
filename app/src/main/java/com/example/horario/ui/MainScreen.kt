package com.example.horario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Grading
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.horario.ui.theme.ThemePalette

private enum class Tab(val title: String) {
    HORARIO("Horario"),
    NOTAS("Notas")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    selectedPalette: ThemePalette,
    onSelectPalette: (ThemePalette) -> Unit,
    themeMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    cardStyle: CardStyle,
    onSelectCardStyle: (CardStyle) -> Unit,
    corner: CornerShape,
    onSelectCorner: (CornerShape) -> Unit,
    density: Density,
    onSelectDensity: (Density) -> Unit
) {
    var tab by remember { mutableStateOf(Tab.HORARIO) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val scheduleVm: ScheduleViewModel = viewModel()
    val gradesVm: GradesViewModel = viewModel()

    // Saludo según la hora del día (toque creativo).
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
                    if (tab == Tab.HORARIO) {
                        Text("$greeting 👋", fontWeight = FontWeight.SemiBold)
                    } else {
                        Text(tab.title, fontWeight = FontWeight.SemiBold)
                    }
                },
                actions = {
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(Icons.Outlined.Palette, contentDescription = "Temas y colores")
                    }
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Cambiar claro/oscuro"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.HORARIO,
                    onClick = { tab = Tab.HORARIO },
                    icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                    label = { Text("Horario") }
                )
                NavigationBarItem(
                    selected = tab == Tab.NOTAS,
                    onClick = { tab = Tab.NOTAS },
                    icon = { Icon(Icons.Outlined.Grading, contentDescription = null) },
                    label = { Text("Notas") }
                )
            }
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
        }
    }

    if (showThemeDialog) {
        ThemeDialog(
            isDark = isDark,
            selectedPalette = selectedPalette,
            onSelectPalette = onSelectPalette,
            themeMode = themeMode,
            onSelectMode = onSelectMode,
            cardStyle = cardStyle,
            onSelectCardStyle = onSelectCardStyle,
            corner = corner,
            onSelectCorner = onSelectCorner,
            density = density,
            onSelectDensity = onSelectDensity,
            onDismiss = { showThemeDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeDialog(
    isDark: Boolean,
    selectedPalette: ThemePalette,
    onSelectPalette: (ThemePalette) -> Unit,
    themeMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    cardStyle: CardStyle,
    onSelectCardStyle: (CardStyle) -> Unit,
    corner: CornerShape,
    onSelectCorner: (CornerShape) -> Unit,
    density: Density,
    onSelectDensity: (Density) -> Unit,
    onDismiss: () -> Unit
) {
    val scroll = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apariencia") },
        text = {
            Column(Modifier.verticalScroll(scroll)) {
                Text("Modo", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeChip("Sistema", themeMode == ThemeMode.SYSTEM) { onSelectMode(ThemeMode.SYSTEM) }
                    ModeChip("Claro", themeMode == ThemeMode.LIGHT) { onSelectMode(ThemeMode.LIGHT) }
                    ModeChip("Oscuro", themeMode == ThemeMode.DARK) { onSelectMode(ThemeMode.DARK) }
                }

                Spacer(Modifier.height(16.dp))
                Text("Forma de esquinas", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                FlowChips {
                    CornerShape.entries.forEach { c ->
                        ModeChip(c.label, corner == c) { onSelectCorner(c) }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Estilo de tarjetas", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                FlowChips {
                    ModeChip("Normal", cardStyle == CardStyle.NORMAL) { onSelectCardStyle(CardStyle.NORMAL) }
                    ModeChip("Glass ✨", cardStyle == CardStyle.GLASS) { onSelectCardStyle(CardStyle.GLASS) }
                    ModeChip("Plano", cardStyle == CardStyle.FLAT) { onSelectCardStyle(CardStyle.FLAT) }
                    ModeChip("Contorno", cardStyle == CardStyle.OUTLINE) { onSelectCardStyle(CardStyle.OUTLINE) }
                    ModeChip("Neón", cardStyle == CardStyle.NEON) { onSelectCardStyle(CardStyle.NEON) }
                }

                Spacer(Modifier.height(16.dp))
                Text("Densidad", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeChip("Cómodo", density == Density.COMFY) { onSelectDensity(Density.COMFY) }
                    ModeChip("Compacto", density == Density.COMPACT) { onSelectDensity(Density.COMPACT) }
                }

                Spacer(Modifier.height(16.dp))
                Text("Color", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(170.dp)
                ) {
                    items(ThemePalette.entries) { palette ->
                        val color = if (isDark) palette.primaryDark else palette.primaryLight
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (palette == selectedPalette) 3.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                    .clickable { onSelectPalette(palette) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (palette == selectedPalette) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Text(
                                palette.displayName,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Listo") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowChips(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) { content() }
}
