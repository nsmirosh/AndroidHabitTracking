package com.learnkmp.habittrackerandroid.data.local

import com.learnkmp.habittrackerandroid.domain.model.Habit
import com.learnkmp.habittrackerandroid.domain.model.HabitType
import java.time.LocalDate
import java.time.LocalTime

fun HabitEntity.toDomain(progress: Int = 0): Habit = Habit(
    id = id,
    name = name,
    type = try { HabitType.valueOf(type) } catch (_: Exception) { HabitType.TIMES_PER_DAY },
    targetCount = targetCount,
    createdDate = runCatching { LocalDate.parse(createdDate) }.getOrDefault(LocalDate.now()),
    reminderTime = reminderTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() },
    progress = progress,
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    type = type.name,
    targetCount = targetCount,
    createdDate = createdDate.toString(),
    reminderTime = reminderTime?.toString(),
)
