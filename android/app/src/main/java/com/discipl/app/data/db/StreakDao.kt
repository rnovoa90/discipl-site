package com.discipl.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.discipl.app.data.model.Streak
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    @Query("SELECT * FROM streak WHERE endDate IS NULL ORDER BY startDate DESC LIMIT 1")
    suspend fun getActiveStreak(): Streak?

    @Query("SELECT * FROM streak WHERE endDate IS NULL ORDER BY startDate DESC LIMIT 1")
    fun observeActiveStreak(): Flow<Streak?>

    @Query("SELECT * FROM streak ORDER BY startDate DESC")
    suspend fun getAll(): List<Streak>

    @Query("SELECT * FROM streak ORDER BY startDate DESC")
    fun observeAll(): Flow<List<Streak>>

    @Insert
    suspend fun insert(streak: Streak)

    @Update
    suspend fun update(streak: Streak)
}
