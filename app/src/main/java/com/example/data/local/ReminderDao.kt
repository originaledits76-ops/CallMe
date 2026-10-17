package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY targetTimestamp ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY targetTimestamp ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY targetTimestamp ASC")
    suspend fun getActiveRemindersList(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderFlow(id: Long): Flow<ReminderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

    @Query("UPDATE reminders SET targetTimestamp = :newTimestamp, isSnoozed = :isSnoozed WHERE id = :id")
    suspend fun updateTargetTimestamp(id: Long, newTimestamp: Long, isSnoozed: Boolean)

    @Query("UPDATE reminders SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setEnabled(id: Long, isEnabled: Boolean)
}
