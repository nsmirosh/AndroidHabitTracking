package com.learnkmp.habittrackerandroid.data.local

import com.learnkmp.habittrackerandroid.domain.model.Habit
import java.time.LocalDate

fun HabitEntity.toDomain(): Habit {
    val today = LocalDate.now().toString()
    return Habit(
        id = id,
        name = name,
        // completedToday resets automatically when the stored date differs from today
        completedToday = completedToday && lastCompletedDate == today,
    )
}

fun Habit.toEntity(): HabitEntity {
    val today = LocalDate.now().toString()
    return HabitEntity(
        id = id,
        name = name,
        completedToday = completedToday,
        lastCompletedDate = if (completedToday) today else null,
    )
}
