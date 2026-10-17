package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.CallMeDatabase
import com.example.data.model.RepeatType
import com.example.scheduling.ReminderScheduler
import com.example.scheduling.ScheduleCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "System event received: $action. Restoring reminder alarms...")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = CallMeDatabase.getInstance(context)
                val scheduler = ReminderScheduler(context)
                val activeReminders = database.reminderDao().getActiveRemindersList()

                val now = System.currentTimeMillis()
                for (reminder in activeReminders) {
                    if (reminder.targetTimestamp > now) {
                        // Still in future, reschedule exact alarm
                        scheduler.schedule(reminder)
                        Log.d(TAG, "Restored future alarm for reminder ${reminder.id}")
                    } else {
                        // Past due, check if recurring
                        val repeat = try { RepeatType.valueOf(reminder.repeatType) } catch (e: Exception) { RepeatType.NONE }
                        if (repeat != RepeatType.NONE) {
                            val nextOccurrence = ScheduleCalculator.calculateNextOccurrence(
                                originalScheduledTime = reminder.targetTimestamp,
                                repeatType = repeat,
                                customDaysMask = reminder.repeatDaysMask,
                                referenceTimeMillis = now
                            )
                            if (nextOccurrence != null) {
                                database.reminderDao().updateTargetTimestamp(reminder.id, nextOccurrence, isSnoozed = false)
                                scheduler.schedule(reminder.copy(targetTimestamp = nextOccurrence, isSnoozed = false))
                                Log.d(TAG, "Advanced recurring reminder ${reminder.id} to next occurrence: $nextOccurrence")
                            }
                        } else {
                            // Non recurring expired reminder: mark disabled
                            database.reminderDao().setEnabled(reminder.id, false)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring alarms on reboot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
