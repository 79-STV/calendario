package com.example.horario.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.horario.data.ClassItem
import java.util.Calendar

/**
 * Programa / cancela una alarma exacta para avisar antes de cada clase.
 * La clase se repite cada semana, así que siempre apuntamos a la próxima
 * ocurrencia del día correspondiente.
 */
object ReminderScheduler {

    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_TEXT = "extra_text"
    const val EXTRA_ID = "extra_id"

    fun schedule(context: Context, item: ClassItem) {
        if (item.reminderMinutes <= 0) {
            cancel(context, item.id)
            return
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextTriggerMillis(item)

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_ID, item.id)
            putExtra(EXTRA_TITLE, item.name)
            putExtra(
                EXTRA_TEXT,
                buildString {
                    append("Empieza a las ${item.startText}")
                    if (item.room.isNotBlank()) append(" · ${item.room}")
                }
            )
        }
        val pending = PendingIntent.getBroadcast(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAt, pending
            )
        } else {
            // Sin permiso de alarma exacta: usamos una aproximada.
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAt, pending
            )
        }
    }

    fun cancel(context: Context, id: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pending)
    }

    /** Calcula el próximo instante (ms) en que debe sonar el recordatorio. */
    private fun nextTriggerMillis(item: ClassItem): Long {
        val reminderAtMinute = item.startMinutes - item.reminderMinutes
        val hour = reminderAtMinute / 60
        val minute = reminderAtMinute % 60

        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply {
            // Calendar.MONDAY = 2 ... Calendar.SUNDAY = 1
            // item.dayOfWeek: 1 = Lunes ... 7 = Domingo
            val calendarDay = when (item.dayOfWeek) {
                7 -> Calendar.SUNDAY
                else -> item.dayOfWeek + 1
            }
            set(Calendar.DAY_OF_WEEK, calendarDay)
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Si ya pasó esta semana, lo mandamos a la siguiente.
        if (cal.timeInMillis <= now.timeInMillis) {
            cal.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
