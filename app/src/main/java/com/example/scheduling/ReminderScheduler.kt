package com.example.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.ReminderEntity
import com.example.receiver.ReminderAlarmReceiver

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val TAG = "ReminderScheduler"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CALLER_NAME = "extra_caller_name"
        const val ACTION_FIRE_REMINDER = "com.example.ACTION_FIRE_REMINDER"
    }

    /**
     * Checks if exact alarm permission is available and granted.
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Schedules an alarm for the given reminder.
     */
    fun schedule(reminder: ReminderEntity) {
        if (!reminder.isEnabled) {
            cancel(reminder.id)
            return
        }

        val triggerTime = reminder.targetTimestamp
        if (triggerTime <= System.currentTimeMillis()) {
            Log.w(TAG, "Attempted to schedule reminder ${reminder.id} in the past. Skipping direct alarm.")
            return
        }

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_FIRE_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_TITLE, reminder.title)
            putExtra(EXTRA_CALLER_NAME, reminder.callerName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled exact alarm for reminder ${reminder.id} at $triggerTime")
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled fallback while-idle alarm for reminder ${reminder.id} at $triggerTime")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling exact alarm, falling back to setAndAllowWhileIdle", e)
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Failed fallback alarm scheduling", fallbackError)
            }
        }
    }

    /**
     * Cancels any scheduled alarm for the reminder id.
     */
    fun cancel(reminderId: Long) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_FIRE_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for reminder $reminderId")
        }
    }
}
