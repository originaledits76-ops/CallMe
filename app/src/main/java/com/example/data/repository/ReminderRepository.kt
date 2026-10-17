package com.example.data.repository

import android.content.Context
import com.example.data.local.CallMeDatabase
import com.example.data.model.ReminderEntity
import com.example.data.model.ReminderHistoryEntity
import com.example.data.model.RepeatType
import com.example.data.preferences.SettingsRepository
import com.example.scheduling.ReminderScheduler
import com.example.scheduling.ScheduleCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ReminderRepository(
    private val context: Context,
    private val database: CallMeDatabase = CallMeDatabase.getInstance(context),
    private val scheduler: ReminderScheduler = ReminderScheduler(context),
    val settingsRepository: SettingsRepository = SettingsRepository(context)
) {
    private val reminderDao = database.reminderDao()
    private val historyDao = database.historyDao()

    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()
    val activeReminders: Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()
    val allHistory: Flow<List<ReminderHistoryEntity>> = historyDao.getAllHistory()

    suspend fun getReminderById(id: Long): ReminderEntity? = withContext(Dispatchers.IO) {
        reminderDao.getReminderById(id)
    }

    fun getReminderFlow(id: Long): Flow<ReminderEntity?> = reminderDao.getReminderFlow(id)

    suspend fun saveReminder(reminder: ReminderEntity): Long = withContext(Dispatchers.IO) {
        val id = reminderDao.insertReminder(reminder)
        val saved = reminder.copy(id = id)
        scheduler.schedule(saved)
        id
    }

    suspend fun updateReminder(reminder: ReminderEntity) = withContext(Dispatchers.IO) {
        reminderDao.updateReminder(reminder)
        scheduler.cancel(reminder.id)
        if (reminder.isEnabled) {
            scheduler.schedule(reminder)
        }
    }

    suspend fun deleteReminder(id: Long) = withContext(Dispatchers.IO) {
        scheduler.cancel(id)
        reminderDao.deleteReminderById(id)
    }

    suspend fun toggleReminder(id: Long, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        reminderDao.setEnabled(id, isEnabled)
        val reminder = reminderDao.getReminderById(id)
        if (reminder != null) {
            if (isEnabled) {
                // If recurring and past, calculate next
                val nextOccurrence = ScheduleCalculator.calculateNextOccurrence(
                    originalScheduledTime = reminder.targetTimestamp,
                    repeatType = RepeatType.valueOf(reminder.repeatType),
                    customDaysMask = reminder.repeatDaysMask,
                    referenceTimeMillis = System.currentTimeMillis()
                )
                val targetTime = nextOccurrence ?: reminder.targetTimestamp
                if (targetTime > System.currentTimeMillis()) {
                    reminderDao.updateTargetTimestamp(id, targetTime, isSnoozed = false)
                    scheduler.schedule(reminder.copy(targetTimestamp = targetTime, isSnoozed = false, isEnabled = true))
                }
            } else {
                scheduler.cancel(id)
            }
        }
    }

    suspend fun snoozeReminder(id: Long, customMinutes: Int? = null) = withContext(Dispatchers.IO) {
        val reminder = reminderDao.getReminderById(id) ?: return@withContext
        val minutes = customMinutes ?: reminder.snoozeDurationMinutes
        val snoozeTimestamp = ScheduleCalculator.calculateSnoozeTimestamp(minutes)

        reminderDao.updateTargetTimestamp(id, snoozeTimestamp, isSnoozed = true)
        scheduler.schedule(reminder.copy(targetTimestamp = snoozeTimestamp, isSnoozed = true))

        historyDao.insertHistory(
            ReminderHistoryEntity(
                reminderId = id,
                title = reminder.title,
                scheduledTimestamp = reminder.targetTimestamp,
                status = "SNOOZED",
                wasSnoozed = true
            )
        )
    }

    suspend fun completeReminder(id: Long) = withContext(Dispatchers.IO) {
        val reminder = reminderDao.getReminderById(id) ?: return@withContext
        scheduler.cancel(id)

        historyDao.insertHistory(
            ReminderHistoryEntity(
                reminderId = id,
                title = reminder.title,
                scheduledTimestamp = reminder.targetTimestamp,
                status = "COMPLETED",
                wasSnoozed = reminder.isSnoozed
            )
        )

        val repeat = try { RepeatType.valueOf(reminder.repeatType) } catch (e: Exception) { RepeatType.NONE }
        if (repeat != RepeatType.NONE) {
            val nextOccurrence = ScheduleCalculator.calculateNextOccurrence(
                originalScheduledTime = reminder.targetTimestamp,
                repeatType = repeat,
                customDaysMask = reminder.repeatDaysMask,
                referenceTimeMillis = System.currentTimeMillis()
            )
            if (nextOccurrence != null) {
                reminderDao.updateTargetTimestamp(id, nextOccurrence, isSnoozed = false)
                scheduler.schedule(reminder.copy(targetTimestamp = nextOccurrence, isSnoozed = false))
                return@withContext
            }
        }

        // Non-recurring: mark as disabled
        reminderDao.setEnabled(id, false)
    }

    suspend fun dismissReminder(id: Long) = withContext(Dispatchers.IO) {
        val reminder = reminderDao.getReminderById(id) ?: return@withContext
        scheduler.cancel(id)

        historyDao.insertHistory(
            ReminderHistoryEntity(
                reminderId = id,
                title = reminder.title,
                scheduledTimestamp = reminder.targetTimestamp,
                status = "DISMISSED",
                wasSnoozed = reminder.isSnoozed
            )
        )

        val repeat = try { RepeatType.valueOf(reminder.repeatType) } catch (e: Exception) { RepeatType.NONE }
        if (repeat != RepeatType.NONE) {
            val nextOccurrence = ScheduleCalculator.calculateNextOccurrence(
                originalScheduledTime = reminder.targetTimestamp,
                repeatType = repeat,
                customDaysMask = reminder.repeatDaysMask,
                referenceTimeMillis = System.currentTimeMillis()
            )
            if (nextOccurrence != null) {
                reminderDao.updateTargetTimestamp(id, nextOccurrence, isSnoozed = false)
                scheduler.schedule(reminder.copy(targetTimestamp = nextOccurrence, isSnoozed = false))
                return@withContext
            }
        }

        // Non-recurring: mark as disabled
        reminderDao.setEnabled(id, false)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearAllHistory()
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        val active = reminderDao.getActiveRemindersList()
        active.forEach { scheduler.cancel(it.id) }
        reminderDao.deleteAllReminders()
        historyDao.clearAllHistory()
    }

    suspend fun exportDataJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val remindersArray = JSONArray()
        val allList = reminderDao.getActiveRemindersList()
        for (r in allList) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("description", r.description)
                put("targetTimestamp", r.targetTimestamp)
                put("callerName", r.callerName)
                put("repeatType", r.repeatType)
                put("repeatDaysMask", r.repeatDaysMask)
                put("snoozeDurationMinutes", r.snoozeDurationMinutes)
                put("priority", r.priority)
                put("isEnabled", r.isEnabled)
            }
            remindersArray.put(obj)
        }
        root.put("reminders", remindersArray)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("version", 1)
        root.toString(2)
    }

    suspend fun importDataJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val remindersArray = root.optJSONArray("reminders") ?: return@withContext false
            for (i in 0 until remindersArray.length()) {
                val obj = remindersArray.getJSONObject(i)
                val reminder = ReminderEntity(
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    targetTimestamp = obj.getLong("targetTimestamp"),
                    callerName = obj.optString("callerName", "Personal Assistant"),
                    repeatType = obj.optString("repeatType", RepeatType.NONE.name),
                    repeatDaysMask = obj.optInt("repeatDaysMask", 0),
                    snoozeDurationMinutes = obj.optInt("snoozeDurationMinutes", 10),
                    priority = obj.optString("priority", "NORMAL"),
                    isEnabled = obj.optBoolean("isEnabled", true)
                )
                saveReminder(reminder)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
