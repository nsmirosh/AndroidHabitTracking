package com.learnkmp.habittrackerandroid.data.local

import androidx.room.Entity

@Entity(
    tableName = "habit_progress",
    primaryKeys = ["habitId", "date"],
)
data class HabitProgressEntity(
    val habitId: String,
    // ISO-8601 date string (e.g. "2026-04-03")
    val date: String,
    val progress: Int,
)
