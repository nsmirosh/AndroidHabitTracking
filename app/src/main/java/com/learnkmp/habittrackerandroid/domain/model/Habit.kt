package com.learnkmp.habittrackerandroid.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Habit(
    val id: String,
    val name: String,
    val type: HabitType = HabitType.TIMES_PER_DAY,
    val targetCount: Int = 1,
    val createdDate: LocalDate = LocalDate.now(),
    val reminderTime: LocalTime? = null,
    val progress: Int = 0,
) {
    val isCompleted: Boolean get() = progress >= targetCount
}
