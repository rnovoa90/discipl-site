package com.discipl.app.service

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shares streak data between the main app and Glance widgets via EncryptedSharedPreferences.
 * Call update() whenever streak data changes (check-in, relapse, launch).
 */
@Singleton
class WidgetDataService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "discipl_widget_data_enc"
        private const val KEY_CURRENT_STREAK_DAYS = "widget_currentStreakDays"
        private const val KEY_STREAK_START_DATE = "widget_streakStartDate"
        private const val KEY_NEXT_MILESTONE_DAY = "widget_nextMilestoneDay"
        private const val KEY_NEXT_MILESTONE_TITLE = "widget_nextMilestoneTitle"
        private const val KEY_LANGUAGE = "widget_language"
    }

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun update(
        currentStreakDays: Int,
        streakStartDate: Long,
        nextMilestoneDay: Int?,
        nextMilestoneTitle: String?,
        language: String
    ) {
        prefs.edit()
            .putInt(KEY_CURRENT_STREAK_DAYS, currentStreakDays)
            .putLong(KEY_STREAK_START_DATE, streakStartDate)
            .putInt(KEY_NEXT_MILESTONE_DAY, nextMilestoneDay ?: 0)
            .putString(KEY_NEXT_MILESTONE_TITLE, nextMilestoneTitle)
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    val currentStreakDays: Int get() = prefs.getInt(KEY_CURRENT_STREAK_DAYS, 0)
    val streakStartDate: Long get() = prefs.getLong(KEY_STREAK_START_DATE, System.currentTimeMillis())
    val nextMilestoneDay: Int? get() = prefs.getInt(KEY_NEXT_MILESTONE_DAY, 0).let { if (it > 0) it else null }
    val nextMilestoneTitle: String? get() = prefs.getString(KEY_NEXT_MILESTONE_TITLE, null)
    val language: String get() = prefs.getString(KEY_LANGUAGE, "es") ?: "es"
}
