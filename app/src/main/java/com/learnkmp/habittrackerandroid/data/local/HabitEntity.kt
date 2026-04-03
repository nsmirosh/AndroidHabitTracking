package com.learnkmp.habittrackerandroid.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val completedToday: Boolean = false,
    // ISO-8601 date string (e.g. "2026-04-03"); null means never completed
    val lastCompletedDate: String? = null,
)
