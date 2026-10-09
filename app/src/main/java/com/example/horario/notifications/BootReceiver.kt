package com.example.horario.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.horario.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Reprograma todos los recordatorios después de reiniciar el teléfono. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val items = AppDatabase.get(context).classDao().getAll()
                items.forEach { ReminderScheduler.schedule(context, it) }
                // Reprograma también el resumen diario de "mañana tienes...".
                DailySummaryScheduler.schedule(context)
            } finally {
                pending.finish()
            }
        }
    }
}
