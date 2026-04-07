package com.learnkmp.habittrackerandroid.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String = "TIMES_PER_DAY",
    val targetCount: Int = 1,
    val progressToday: Int = 0,
    // ISO-8601 date string (e.g. "2026-04-03"); null means never tracked today
    val lastCompletedDate: String? = null,
)
