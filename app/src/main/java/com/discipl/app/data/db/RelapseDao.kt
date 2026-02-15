package com.discipl.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.discipl.app.data.model.Relapse
import kotlinx.coroutines.flow.Flow

@Dao
interface RelapseDao {

    @Query("SELECT * FROM relapse ORDER BY date DESC")
    suspend fun getAll(): List<Relapse>

    @Query("SELECT * FROM relapse ORDER BY date DESC")
    fun observeAll(): Flow<List<Relapse>>

    @Query("SELECT COUNT(*) FROM relapse")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM relapse WHERE date >= :startOfDay AND date < :endOfDay")
    suspend fun countForDay(startOfDay: Long, endOfDay: Long): Int

    @Insert
    suspend fun insert(relapse: Relapse)
}
