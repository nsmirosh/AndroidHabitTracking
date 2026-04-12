package com.learnkmp.habittrackerandroid.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.learnkmp.habittrackerandroid.R

object NotificationHelper {
    const val CHANNEL_ID = "habit_reminders"
    private const val CHANNEL_NAME = "Habit reminders"
    private const val CHANNEL_DESCRIPTION = "Daily reminders for your habits"

    fun ensureChannel(context: Context) {
        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = CHANNEL_DESCRIPTION }
        manager.createNotificationChannel(channel)
    }

    fun showHabitReminder(context: Context, habitId: String, habitName: String) {
        ensureChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(habitName)
            .setContentText("Time to check in on your habit")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(habitId.hashCode(), notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted; silently drop
        }
    }
}
