package com.discipl.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.discipl.app.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun get(): UserProfile?

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observe(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfile)

    @Update
    suspend fun update(profile: UserProfile)
}
