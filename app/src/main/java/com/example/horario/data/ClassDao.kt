package com.example.horario.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {

    @Query("SELECT * FROM classes ORDER BY dayOfWeek, startMinutes")
    fun observeAll(): Flow<List<ClassItem>>

    @Query("SELECT * FROM classes ORDER BY dayOfWeek, startMinutes")
    suspend fun getAll(): List<ClassItem>

    @Query("SELECT * FROM classes WHERE id = :id")
    suspend fun getById(id: Long): ClassItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClassItem): Long

    @Update
    suspend fun update(item: ClassItem)

    @Delete
    suspend fun delete(item: ClassItem)
}
