package com.learnkmp.habittrackerandroid.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != HabitReminderScheduler.ACTION_HABIT_REMINDER) return
        val habitId = intent.getStringExtra(HabitReminderScheduler.EXTRA_HABIT_ID) ?: return
        val habitName = intent.getStringExtra(HabitReminderScheduler.EXTRA_HABIT_NAME) ?: return
        NotificationHelper.showHabitReminder(context, habitId, habitName)
    }
}
