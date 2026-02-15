package com.discipl.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.discipl.app.data.model.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {

    @Query("SELECT * FROM journal_entry ORDER BY date DESC")
    suspend fun getAll(): List<JournalEntry>

    @Query("SELECT * FROM journal_entry ORDER BY date DESC")
    fun observeAll(): Flow<List<JournalEntry>>

    @Insert
    suspend fun insert(entry: JournalEntry)
}
