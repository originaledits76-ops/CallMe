package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ReminderHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM reminder_history ORDER BY actionTimestamp DESC")
    fun getAllHistory(): Flow<List<ReminderHistoryEntity>>

    @Query("SELECT * FROM reminder_history ORDER BY actionTimestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<ReminderHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: ReminderHistoryEntity): Long

    @Query("DELETE FROM reminder_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM reminder_history")
    suspend fun clearAllHistory()
}
