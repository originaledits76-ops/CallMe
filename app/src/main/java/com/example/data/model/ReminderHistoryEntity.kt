package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_history")
data class ReminderHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reminderId: Long? = null,
    val title: String,
    val scheduledTimestamp: Long,
    val actionTimestamp: Long = System.currentTimeMillis(),
    val status: String, // COMPLETED, SNOOZED, DISMISSED, MISSED
    val wasSnoozed: Boolean = false
)
