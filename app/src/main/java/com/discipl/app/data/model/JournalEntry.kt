package com.discipl.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "journal_entry")
data class JournalEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val mood: Int = 3,             // 1-5 scale
    val feelings: String? = null,  // comma-separated: "anxious,lonely,grateful"
    val energyLevel: Int? = null,  // 1-5 (very low → very high)
    val notes: String? = null
) {
    companion object {
        val feelingOptions = listOf(
            "anxious", "calm", "motivated", "lonely",
            "grateful", "stressed", "happy", "frustrated",
            "bored", "confident"
        )
    }
}
