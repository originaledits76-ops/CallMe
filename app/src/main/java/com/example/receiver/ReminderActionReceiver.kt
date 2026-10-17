package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.repository.ReminderRepository
import com.example.notification.ReminderNotificationManager
import com.example.ui.call.IncomingCallActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ReminderActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val reminderId = intent.getLongExtra(ReminderNotificationManager.EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        Log.d(TAG, "Received action: $action for reminder: $reminderId")

        val notificationManager = ReminderNotificationManager(context)
        val repository = ReminderRepository(context)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ReminderNotificationManager.ACTION_ANSWER -> {
                        notificationManager.dismissNotification(reminderId)
                        val activityIntent = Intent(context, IncomingCallActivity::class.java).apply {
                            putExtra(ReminderNotificationManager.EXTRA_REMINDER_ID, reminderId)
                            putExtra("auto_answer", true)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        context.startActivity(activityIntent)
                    }
                    ReminderNotificationManager.ACTION_SNOOZE -> {
                        notificationManager.dismissNotification(reminderId)
                        repository.snoozeReminder(reminderId)
                    }
                    ReminderNotificationManager.ACTION_DISMISS -> {
                        notificationManager.dismissNotification(reminderId)
                        repository.dismissReminder(reminderId)
                    }
                    ReminderNotificationManager.ACTION_COMPLETE -> {
                        notificationManager.dismissNotification(reminderId)
                        repository.completeReminder(reminderId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error executing action $action", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
