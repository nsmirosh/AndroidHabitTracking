package com.learnkmp.habittrackerandroid.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.learnkmp.habittrackerandroid.domain.model.Habit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(habit: Habit) {
        val time = habit.reminderTime ?: return cancel(habit.id)
        val triggerAtMillis = nextTriggerMillis(time)
        val pending = PendingIntent.getBroadcast(
            context,
            habit.id.hashCode(),
            buildIntent(habit.id, habit.name),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            AlarmManager.INTERVAL_DAY,
            pending,
        )
    }

    fun cancel(habitId: String) {
        val pending = PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            buildIntent(habitId, habitName = ""),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pending)
        pending.cancel()
    }

    private fun buildIntent(habitId: String, habitName: String): Intent =
        Intent(context, HabitReminderReceiver::class.java).apply {
            action = ACTION_HABIT_REMINDER
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra(EXTRA_HABIT_NAME, habitName)
            data = android.net.Uri.parse("habit://reminder/$habitId")
        }

    private fun nextTriggerMillis(time: LocalTime): Long {
        val now = LocalDateTime.now()
        var next = now.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    companion object {
        const val ACTION_HABIT_REMINDER = "com.learnkmp.habittrackerandroid.HABIT_REMINDER"
        const val EXTRA_HABIT_ID = "habit_id"
        const val EXTRA_HABIT_NAME = "habit_name"
    }
}
