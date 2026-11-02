package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HabitDeadlineReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitName = intent.getStringExtra("EXTRA_HABIT_NAME") ?: "Daily Habit"
        val squadName = intent.getStringExtra("EXTRA_SQUAD_NAME") ?: "Habit Circle"
        val hoursRemaining = intent.getIntExtra("EXTRA_HOURS_REMAINING", 1)

        val notificationManager = StreakNotificationManager(context)
        notificationManager.sendDeadlineReminderNotification(
            habitName = habitName,
            squadName = squadName,
            hoursRemaining = hoursRemaining
        )
    }
}
