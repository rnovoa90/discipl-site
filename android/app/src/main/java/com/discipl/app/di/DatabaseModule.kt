package com.discipl.app.di

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val passphrase = getOrCreatePassphrase(context)
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "discipl.db"
        )
            .openHelperFactory(factory)
            .build()
    }

    private fun getOrCreatePassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            "discipl_db_key",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val key = "db_passphrase"
        var passphrase = prefs.getString(key, null)
        if (passphrase == null) {
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            passphrase = bytes.joinToString("") { "%02x".format(it) }
            prefs.edit().putString(key, passphrase).apply()
        }
        return passphrase.toByteArray()
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
