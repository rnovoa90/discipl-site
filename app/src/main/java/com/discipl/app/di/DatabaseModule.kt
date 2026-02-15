package com.discipl.app.di

import android.content.Context
import androidx.room.Room
import com.discipl.app.data.db.AppDatabase
import com.discipl.app.data.db.DailyCheckInDao
import com.discipl.app.data.db.JournalEntryDao
import com.discipl.app.data.db.RelapseDao
import com.discipl.app.data.db.StreakDao
import com.discipl.app.data.db.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "discipl.db"
        ).build()
    }

    @Provides
    fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideStreakDao(db: AppDatabase): StreakDao = db.streakDao()

    @Provides
    fun provideRelapseDao(db: AppDatabase): RelapseDao = db.relapseDao()

    @Provides
    fun provideDailyCheckInDao(db: AppDatabase): DailyCheckInDao = db.dailyCheckInDao()

    @Provides
    fun provideJournalEntryDao(db: AppDatabase): JournalEntryDao = db.journalEntryDao()
}
