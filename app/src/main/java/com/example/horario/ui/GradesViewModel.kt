package com.example.horario.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.horario.data.Activity
import com.example.horario.data.AppDatabase
import com.example.horario.data.Subject
import com.example.horario.data.SubjectWithActivities
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GradesViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app.applicationContext).gradesDao()

    val subjects: StateFlow<List<SubjectWithActivities>> =
        combine(dao.observeSubjects(), dao.observeActivities()) { subjects, activities ->
            subjects.map { subject ->
                SubjectWithActivities(
                    subject = subject,
                    activities = activities.filter { it.subjectId == subject.id }
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubject(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { dao.insertSubject(Subject(name = name.trim())) }
    }

    fun renameSubject(subject: Subject, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { dao.updateSubject(subject.copy(name = newName.trim())) }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch { dao.deleteSubject(subject) }
    }

    fun addActivity(subjectId: Long, name: String, grade: Double, weight: Double) {
        viewModelScope.launch {
            dao.insertActivity(
                Activity(
                    subjectId = subjectId,
                    name = name.trim().ifBlank { "Actividad" },
                    grade = grade,
                    weightPercent = weight
                )
            )
        }
    }

    fun updateActivity(activity: Activity) {
        viewModelScope.launch { dao.updateActivity(activity) }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch { dao.deleteActivity(activity) }
    }
}
