package com.example.horario.data

import android.content.Context
import com.example.horario.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow

/** Punto único para acceder a las clases y mantener los recordatorios en sincronía. */
class ClassRepository(private val context: Context) {

    private val dao = AppDatabase.get(context).classDao()

    fun observeAll(): Flow<List<ClassItem>> = dao.observeAll()

    suspend fun save(item: ClassItem): Long {
        val id = if (item.id == 0L) {
            dao.insert(item)
        } else {
            dao.update(item)
            item.id
        }
        val saved = item.copy(id = id)
        ReminderScheduler.schedule(context, saved)
        return id
    }

    suspend fun delete(item: ClassItem) {
        dao.delete(item)
        ReminderScheduler.cancel(context, item.id)
    }
}
