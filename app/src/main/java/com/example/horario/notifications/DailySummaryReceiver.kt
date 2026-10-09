package com.example.horario.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.horario.MainActivity
import com.example.horario.R
import com.example.horario.data.AppDatabase
import com.example.horario.data.ClassItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Se dispara una vez al día (8 PM). Arma el resumen de las clases de MAÑANA
 * y, al final, se reprograma para el día siguiente.
 */
class DailySummaryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tomorrow = tomorrowDayOfWeek()
                val classes = AppDatabase.get(context).classDao().getAll()
                    .filter { it.dayOfWeek == tomorrow }
                    .sortedBy { it.startMinutes }

                if (classes.isNotEmpty()) {
                    showSummary(context, classes)
                }
            } finally {
                // Reprogramar para el día siguiente
                DailySummaryScheduler.schedule(context)
                pending.finish()
            }
        }
    }

    /** 1 = Lunes ... 7 = Domingo, correspondiente a MAÑANA. */
    private fun tomorrowDayOfWeek(): Int {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 7
        }
    }

    private fun showSummary(context: Context, classes: List<ClassItem>) {
        val channelId = "daily_summary"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Resumen de mañana",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Aviso la noche anterior con las clases del día siguiente" }
            manager.createNotificationChannel(channel)
        }

        val lines = classes.joinToString("\n") { c ->
            buildString {
                append("• ${c.startText} ")
                append(c.name)
                if (c.room.isNotBlank()) append(" · ${c.room}")
            }
        }
        val title = "📅 Mañana tienes ${classes.size} ${if (classes.size == 1) "clase" else "clases"}"

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = android.app.PendingIntent.getActivity(
            context, 999002, openIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(classes.joinToString("  ·  ") { "${it.startText} ${it.name}" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(lines))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentPending)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(999002, notification)
    }
}
