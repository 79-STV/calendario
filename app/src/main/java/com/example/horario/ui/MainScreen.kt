package com.example.horario.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Grading
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private enum class Tab(val title: String) {
    HORARIO("Horario"),
    NOTAS("Notas")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    var tab by remember { mutableStateOf(Tab.HORARIO) }
    var showImport by remember { mutableStateOf(false) }

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
                    if (tab == Tab.HORARIO) {
                        IconButton(onClick = { showImport = true }) {
                            Icon(
                                Icons.Outlined.AddAPhoto,
                                contentDescription = "Importar desde foto"
                            )
                        }
                    }
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Cambiar tema"
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
        androidx.compose.foundation.layout.Box(
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

            // Pantalla de importación por foto, superpuesta sobre el contenido.
            if (showImport) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    ImportPhotoScreen(
                        onCancel = { showImport = false },
                        onConfirm = { items ->
                            scheduleVm.saveAll(items)
                            showImport = false
                        }
                    )
                }
            }
        }
    }
}
