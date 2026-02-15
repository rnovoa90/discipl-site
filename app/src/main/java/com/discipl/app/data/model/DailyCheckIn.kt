package com.discipl.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "daily_check_in")
data class DailyCheckIn(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val status: String = STATUS_CLEAN // "clean" or "relapsed"
) {
    companion object {
        const val STATUS_CLEAN = "clean"
        const val STATUS_RELAPSED = "relapsed"
    }
}
