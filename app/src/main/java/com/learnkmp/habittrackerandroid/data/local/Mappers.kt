package com.learnkmp.habittrackerandroid.data.local

import com.learnkmp.habittrackerandroid.domain.model.Habit
import com.learnkmp.habittrackerandroid.domain.model.HabitType
import java.time.LocalDate

fun HabitEntity.toDomain(): Habit {
    val today = LocalDate.now().toString()
    // Reset progress when the stored date differs from today
    val effectiveProgress = if (lastCompletedDate == today) progressToday else 0
    return Habit(
        id = id,
        name = name,
        type = try { HabitType.valueOf(type) } catch (_: Exception) { HabitType.TIMES_PER_DAY },
        targetCount = targetCount,
        progressToday = effectiveProgress,
    )
}

fun Habit.toEntity(): HabitEntity {
    val today = LocalDate.now().toString()
    return HabitEntity(
        id = id,
        name = name,
        type = type.name,
        targetCount = targetCount,
        progressToday = progressToday,
        lastCompletedDate = if (progressToday > 0) today else null,
    )
}
