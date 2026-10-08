package com.example.horario.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Una materia dentro del apartado de Notas (ej. "Cálculo Diferencial"). */
@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

/**
 * Una actividad calificable de una materia.
 * Modelo simple: nota (1.0–5.0) + porcentaje que vale sobre el total.
 *
 * @param grade nota obtenida (0.0 si aún no hay nota)
 * @param weightPercent porcentaje que vale esta actividad (ej. 35.0)
 */
@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val name: String,
    val grade: Double = 0.0,
    val weightPercent: Double = 0.0
)

/** Una materia junto con sus actividades y los cálculos ya resueltos. */
data class SubjectWithActivities(
    val subject: Subject,
    val activities: List<Activity>
) {
    /** Suma de los porcentajes registrados (ej. 70.0 si faltan actividades por sumar 100). */
    val totalWeight: Double get() = activities.sumOf { it.weightPercent }

    /** Nota ponderada acumulada = Σ(nota × %/100). */
    val weightedGrade: Double
        get() = activities.sumOf { it.grade * it.weightPercent / 100.0 }

    /**
     * Proyección sobre 100%: cuánto sería la nota si lo registrado representara el total.
     * Útil como "promedio" cuando aún no se llega al 100%.
     */
    val projectedGrade: Double
        get() = if (totalWeight > 0) weightedGrade * 100.0 / totalWeight else 0.0
}
