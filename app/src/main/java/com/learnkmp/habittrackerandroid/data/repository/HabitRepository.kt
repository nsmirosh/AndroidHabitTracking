package com.learnkmp.habittrackerandroid.data.repository

import com.learnkmp.habittrackerandroid.domain.model.Habit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {
    fun observeAll(date: LocalDate): Flow<List<Habit>>
    fun observeById(habitId: String, date: LocalDate): Flow<Habit?>
    suspend fun upsertHabit(habit: Habit)
    suspend fun setProgress(habitId: String, date: LocalDate, progress: Int)
    suspend fun delete(habitId: String)
    suspend fun syncFromFirestore(userId: String)
}
