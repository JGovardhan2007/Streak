package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

class StreakNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_COMPLETIONS = "squad_completions"
        const val CHANNEL_NUDGES = "squad_nudges"
        const val CHANNEL_DEADLINES = "squad_deadlines"

        private var notificationIdCounter = 100
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val completionsChannel = NotificationChannel(
                CHANNEL_COMPLETIONS,
                "Circle Habit Completions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when a circle member completes their daily habit"
                enableVibration(true)
            }

            val nudgesChannel = NotificationChannel(
                CHANNEL_NUDGES,
                "Circle Reminders & Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when a circle member sends you a friendly reminder"
                enableVibration(true)
            }

            val deadlinesChannel = NotificationChannel(
                CHANNEL_DEADLINES,
                "1-Hour Deadline Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent 1-hour reminders before habit deadlines to prevent broken streaks"
                enableVibration(true)
            }

            manager.createNotificationChannel(completionsChannel)
            manager.createNotificationChannel(nudgesChannel)
            manager.createNotificationChannel(deadlinesChannel)
        }
    }

    fun sendSquadCompletionNotification(memberName: String, habitName: String, streakDays: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_COMPLETIONS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎉 $memberName completed $habitName!")
            .setContentText("Circle streak is now 🔥 $streakDays days! Keep the momentum going.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationIdCounter++, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun sendNudgeNotification(fromMemberName: String, habitName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_NUDGES)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚡ Friendly Reminder from $fromMemberName!")
            .setContentText("$fromMemberName sent you a friendly reminder to check in for '$habitName'. Don't break the streak!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationIdCounter++, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun sendDeadlineReminderNotification(habitName: String, squadName: String, hoursRemaining: Int = 1) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DEADLINES)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 1-Hour Deadline Warning: $habitName!")
            .setContentText("Your $squadName habit circle has 1 hour left before deadline! Check in now to save your streak 🔥")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationIdCounter++, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun scheduleDeadlineReminder(
        habitId: String,
        habitName: String,
        squadName: String,
        deadlineHoursRemaining: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, HabitDeadlineReceiver::class.java).apply {
            putExtra("EXTRA_HABIT_NAME", habitName)
            putExtra("EXTRA_SQUAD_NAME", squadName)
            putExtra("EXTRA_HOURS_REMAINING", 1)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Trigger time set to 1 hour before the habit deadline
        // (For testing purposes / immediate simulation when deadline is near, set trigger within 10 seconds if <= 2 hours remaining)
        val triggerTimeMillis = System.currentTimeMillis() + if (deadlineHoursRemaining <= 2) 10_000L else ((deadlineHoursRemaining - 1) * 3600_000L)

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
