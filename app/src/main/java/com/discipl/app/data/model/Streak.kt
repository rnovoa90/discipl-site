package com.discipl.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

@Entity(tableName = "streak")
data class Streak(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null // null = current active streak
) {
    /** Whether this streak is currently active (no end date). */
    val isActive: Boolean get() = endDate == null

    /**
     * Calculates duration in days from startDate to endDate (or today if active).
     * Uses start-of-day truncation to match iOS Calendar.startOfDay behavior.
     */
    val durationDays: Int
        get() {
            val zone = ZoneId.systemDefault()
            val startDay = Instant.ofEpochMilli(startDate)
                .atZone(zone)
                .toLocalDate()
            val endDay = if (endDate != null) {
                Instant.ofEpochMilli(endDate)
                    .atZone(zone)
                    .toLocalDate()
            } else {
                LocalDate.now()
            }
            return maxOf(0, ChronoUnit.DAYS.between(startDay, endDay).toInt())
        }
}
