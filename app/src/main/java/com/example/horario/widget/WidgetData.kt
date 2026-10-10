package com.example.horario.widget

import android.content.Context
import com.example.horario.data.AppDatabase
import com.example.horario.data.ClassItem
import java.util.Calendar

/** Qué mostrar en el widget según la hora actual. */
data class WidgetState(
    val headline: String,   // "Clase actual" / "Próxima clase" / "Hoy"
    val title: String,      // nombre de la materia o mensaje
    val subtitle: String    // hora + aula, o vacío
)

object WidgetData {

    suspend fun load(context: Context): WidgetState {
        val todayDow = currentDayOfWeek()
        val nowMin = currentMinutes()

        val classes = AppDatabase.get(context).classDao().getAll()
            .filter { it.dayOfWeek == todayDow }
            .sortedBy { it.startMinutes }

        if (classes.isEmpty()) {
            return WidgetState("Hoy", "Sin clases", "")
        }

        // ¿Hay una clase en curso?
        val current = classes.firstOrNull { nowMin in it.startMinutes until it.endMinutes }
        if (current != null) {
            return WidgetState(
                headline = "Clase actual",
                title = current.name,
                subtitle = line(current)
            )
        }

        // Si no, la próxima de hoy.
        val next = classes.firstOrNull { it.startMinutes > nowMin }
        if (next != null) {
            return WidgetState(
                headline = "Próxima clase",
                title = next.name,
                subtitle = line(next)
            )
        }

        return WidgetState("Hoy", "No quedan clases", "")
    }

    private fun line(c: ClassItem): String = buildString {
        append("${c.startText} - ${c.endText}")
        if (c.room.isNotBlank()) append("  ·  ${c.room}")
    }

    private fun currentDayOfWeek(): Int = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        else -> 7
    }

    private fun currentMinutes(): Int {
        val c = Calendar.getInstance()
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
    }
}
