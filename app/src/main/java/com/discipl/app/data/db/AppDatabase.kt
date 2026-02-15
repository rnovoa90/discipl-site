package com.discipl.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.discipl.app.data.model.DailyCheckIn
import com.discipl.app.data.model.JournalEntry
import com.discipl.app.data.model.Relapse
import com.discipl.app.data.model.Streak
import com.discipl.app.data.model.UserProfile

@Database(
    entities = [
        UserProfile::class,
        Streak::class,
        Relapse::class,
        DailyCheckIn::class,
        JournalEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun streakDao(): StreakDao
    abstract fun relapseDao(): RelapseDao
    abstract fun dailyCheckInDao(): DailyCheckInDao
    abstract fun journalEntryDao(): JournalEntryDao
}
