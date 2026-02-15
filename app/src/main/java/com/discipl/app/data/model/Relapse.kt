package com.discipl.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "relapse")
data class Relapse(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val timeOfDay: String? = null,  // "morning", "afternoon", "evening", "late_night"
    val trigger: String? = null,    // "boredom", "stress", "loneliness", "late_night", "social_media", "alcohol", "other"
    val moodBefore: Int? = null,    // 1-5 scale
    val notes: String? = null
) {
    companion object {
        val timeOfDayOptions = listOf("morning", "afternoon", "evening", "late_night")
        val triggerOptions = listOf("boredom", "stress", "loneliness", "late_night", "social_media", "alcohol", "other")
    }
}
