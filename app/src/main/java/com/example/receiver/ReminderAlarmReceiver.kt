package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.CallMeDatabase
import com.example.notification.ReminderNotificationManager
import com.example.scheduling.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ReminderAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) {
            Log.w(TAG, "Received alarm intent without valid reminder ID")
            return
        }

        Log.d(TAG, "Alarm triggered for reminder ID: $reminderId")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = CallMeDatabase.getInstance(context)
                val reminder = database.reminderDao().getReminderById(reminderId)

                if (reminder != null && reminder.isEnabled) {
                    val notificationManager = ReminderNotificationManager(context)
                    notificationManager.showIncomingCallNotification(reminder)
                } else {
                    Log.d(TAG, "Reminder $reminderId is disabled or not found; ignoring trigger.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling alarm trigger", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
