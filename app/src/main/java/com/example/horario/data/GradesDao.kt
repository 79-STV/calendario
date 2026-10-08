package com.example.horario.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GradesDao {

    // --- Materias ---
    @Query("SELECT * FROM subjects ORDER BY name")
    fun observeSubjects(): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    // --- Actividades ---
    @Query("SELECT * FROM activities ORDER BY id")
    fun observeActivities(): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE subjectId = :subjectId ORDER BY id")
    fun observeActivitiesFor(subjectId: Long): Flow<List<Activity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity): Long

    @Update
    suspend fun updateActivity(activity: Activity)

    @Delete
    suspend fun deleteActivity(activity: Activity)
}
