package com.learnkmp.habittrackerandroid

import android.app.Application
import com.learnkmp.habittrackerandroid.reminders.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HabitTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
    }
}
