package com.example.horario.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Una clase del horario semanal.
 *
 * @param dayOfWeek 1 = Lunes ... 7 = Domingo (igual que java.time.DayOfWeek)
 * @param startMinutes minuto del día en que empieza (ej. 8:30 -> 510)
 * @param endMinutes minuto del día en que termina
 * @param colorHex color de la tarjeta, ej. "#7C4DFF"
 * @param reminderMinutes minutos antes para avisar (0 = sin recordatorio)
 */
@Entity(tableName = "classes")
data class ClassItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val room: String = "",
    val dayOfWeek: Int,
    val startMinutes: Int,
    val endMinutes: Int,
    val colorHex: String = "#7C4DFF",
    val reminderMinutes: Int = 10
) {
    val startText: String get() = formatTime(startMinutes)
    val endText: String get() = formatTime(endMinutes)

    companion object {
        fun formatTime(totalMinutes: Int): String {
            val h = totalMinutes / 60
            val m = totalMinutes % 60
            return "%02d:%02d".format(h, m)
        }
    }
}
