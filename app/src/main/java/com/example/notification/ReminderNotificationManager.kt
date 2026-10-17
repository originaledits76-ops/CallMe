package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R
import com.example.data.model.ReminderEntity
import com.example.receiver.ReminderActionReceiver
import com.example.ui.call.IncomingCallActivity

class ReminderNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "callme_incoming_calls_channel"
        const val CHANNEL_NAME = "Incoming Reminder Calls"
        const val NOTIFICATION_ID_BASE = 10000

        const val ACTION_ANSWER = "com.example.ACTION_ANSWER"
        const val ACTION_SNOOZE = "com.example.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.example.ACTION_DISMISS"
        const val ACTION_COMPLETE = "com.example.ACTION_COMPLETE"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CALLER_NAME = "extra_caller_name"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent incoming call alerts for scheduled reminders"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showIncomingCallNotification(reminder: ReminderEntity) {
        val notificationId = (NOTIFICATION_ID_BASE + reminder.id).toInt()

        // Intent for opening the full-screen IncomingCallActivity
        val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification Action: Answer
        val answerIntent = Intent(context, IncomingCallActivity::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra("auto_answer", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val answerPendingIntent = PendingIntent.getActivity(
            context,
            (reminder.id * 10 + 1).toInt(),
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification Action: Snooze
        val snoozeIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (reminder.id * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification Action: Dismiss
        val dismissIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            (reminder.id * 10 + 3).toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val caller = if (reminder.callerName.isNotBlank()) reminder.callerName else "Personal Assistant"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Incoming Call: $caller")
            .setContentText(reminder.title)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${reminder.title}\n${reminder.description}"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_launcher_foreground, "Answer", answerPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Snooze (${reminder.snoozeDurationMinutes}m)", snoozePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Dismiss", dismissPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Notification permission might not be granted yet
        }
    }

    fun dismissNotification(reminderId: Long) {
        val notificationId = (NOTIFICATION_ID_BASE + reminderId).toInt()
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}
