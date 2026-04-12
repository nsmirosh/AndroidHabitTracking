package com.learnkmp.habittrackerandroid.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String = "TIMES_PER_DAY",
    val targetCount: Int = 1,
    // ISO-8601 date string (e.g. "2026-04-11")
    val createdDate: String,
    // ISO-8601 local time string (e.g. "08:30"); null means no reminder
    val reminderTime: String? = null,
)
