package com.learnkmp.habittrackerandroid.domain.model

data class Habit(
    val id: String,
    val name: String,
    val type: HabitType = HabitType.TIMES_PER_DAY,
    val targetCount: Int = 1,
    val progressToday: Int = 0,
) {
    val completedToday: Boolean get() = progressToday >= targetCount
}
