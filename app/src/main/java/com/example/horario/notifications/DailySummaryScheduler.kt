package com.example.horario.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Programa un aviso diario (por defecto 8:00 PM) que resume las clases de MAÑANA:
 * "Mañana tienes: Cálculo 12:00 · Álgebra 10:00...".
 */
object DailySummaryScheduler {

    const val SUMMARY_HOUR = 20   // 8:00 PM
    const val SUMMARY_MINUTE = 0
    private const val REQUEST_CODE = 999001

    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = buildPending(context)
        val triggerAt = nextTriggerMillis()

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    private fun buildPending(context: Context): PendingIntent {
        val intent = Intent(context, DailySummaryReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerMillis(): Long {
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, SUMMARY_HOUR)
            set(Calendar.MINUTE, SUMMARY_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now.timeInMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
