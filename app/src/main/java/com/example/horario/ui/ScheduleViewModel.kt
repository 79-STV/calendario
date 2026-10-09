package com.example.horario.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.horario.data.ClassItem
import com.example.horario.data.ClassRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduleViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ClassRepository(app.applicationContext)

    /** Clases agrupadas por día de la semana (1..7). */
    val classesByDay: StateFlow<Map<Int, List<ClassItem>>> =
        repo.observeAll()
            .map { list -> list.groupBy { it.dayOfWeek } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun save(item: ClassItem) {
        viewModelScope.launch { repo.save(item) }
    }

    /** Guarda varias clases de golpe (usado al importar desde foto). */
    fun saveAll(items: List<ClassItem>) {
        viewModelScope.launch { items.forEach { repo.save(it) } }
    }

    fun delete(item: ClassItem) {
        viewModelScope.launch { repo.delete(item) }
    }
}
