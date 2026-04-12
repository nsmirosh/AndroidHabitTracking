package com.learnkmp.habittrackerandroid.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.learnkmp.habittrackerandroid.data.local.HabitDao
import com.learnkmp.habittrackerandroid.data.local.toDomain
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var habitDao: HabitDao
    @Inject lateinit var scheduler: HabitReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val today = LocalDate.now().toString()
                val habits = habitDao.observeAllCreatedOnOrBefore(today).first()
                habits.forEach { entity ->
                    val habit = entity.toDomain()
                    if (habit.reminderTime != null) scheduler.schedule(habit)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
