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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(ReminderScheduler.EXTRA_ID, 0L)
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: "Clase"
        val text = intent.getStringExtra(ReminderScheduler.EXTRA_TEXT) ?: "Tu clase está por empezar"

        showNotification(context, id.toInt(), title, text)

        // Reprograma para la próxima semana (recordatorio semanal recurrente).
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val item = AppDatabase.get(context).classDao().getById(id)
                if (item != null) {
                    ReminderScheduler.schedule(context, item)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun showNotification(context: Context, notifId: Int, title: String, text: String) {
        val channelId = "class_reminders"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de clase",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Avisos antes de que empiece una clase" }
            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = android.app.PendingIntent.getActivity(
            context,
            notifId,
            openIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
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
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }
}
