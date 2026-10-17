package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val targetTimestamp: Long,
    val callerName: String = "Personal Assistant",
    val voiceRecordingPath: String? = null,
    val repeatType: String = RepeatType.NONE.name,
    val repeatDaysMask: Int = 0, // Bitmask for Mon(1), Tue(2), Wed(4), Thu(8), Fri(16), Sat(32), Sun(64)
    val snoozeDurationMinutes: Int = 10,
    val priority: String = ReminderPriority.NORMAL.name,
    val isEnabled: Boolean = true,
    val isSnoozed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
