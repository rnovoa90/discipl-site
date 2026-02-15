package com.discipl.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val quitDate: Long = System.currentTimeMillis(),
    val quitType: String = "both", // "porn", "masturbation", "both"
    val language: String = "es",   // "es" or "en"
    val createdAt: Long = System.currentTimeMillis(),
    val isPremium: Boolean = false,
    val dailyReminderTime: Long? = null,
    val hasCompletedOnboarding: Boolean = false,

    // Notification preferences
    val morningMotivationEnabled: Boolean = true,
    val morningMotivationTime: Long = 28800000L, // 08:00 as millis from midnight
    val milestoneNotificationsEnabled: Boolean = true,
    val eveningCheckInEnabled: Boolean = false,
    val eveningCheckInTime: Long = 75600000L,     // 21:00 as millis from midnight
    val reengagementEnabled: Boolean = true,

    // Feature preferences
    val dailyTaskEnabled: Boolean = true,
    val dailyInsightEnabled: Boolean = true
)
