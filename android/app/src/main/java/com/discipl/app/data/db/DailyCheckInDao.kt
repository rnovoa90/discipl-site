package com.discipl.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.discipl.app.data.model.DailyCheckIn
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCheckInDao {

    @Query("SELECT COUNT(*) FROM daily_check_in WHERE date >= :startOfDay AND date < :endOfDay")
    suspend fun countForDay(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT * FROM daily_check_in WHERE date >= :startOfMonth AND date < :endOfMonth ORDER BY date ASC")
    suspend fun getForMonth(startOfMonth: Long, endOfMonth: Long): List<DailyCheckIn>

    @Query("SELECT * FROM daily_check_in WHERE date >= :startOfMonth AND date < :endOfMonth ORDER BY date ASC")
    fun observeForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<DailyCheckIn>>

    @Insert
    suspend fun insert(checkIn: DailyCheckIn)
}
