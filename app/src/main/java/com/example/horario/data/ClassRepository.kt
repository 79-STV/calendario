package com.example.horario.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/** Punto único para acceder a las clases guardadas localmente. */
class ClassRepository(context: Context) {

    private val dao = AppDatabase.get(context).classDao()

    fun observeAll(): Flow<List<ClassItem>> = dao.observeAll()

    suspend fun save(item: ClassItem): Long {
        return if (item.id == 0L) {
            dao.insert(item)
        } else {
            dao.update(item)
            item.id
        }
    }

    suspend fun delete(item: ClassItem) {
        dao.delete(item)
    }
}
