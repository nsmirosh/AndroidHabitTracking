package com.learnkmp.habittrackerandroid.data.repository

import com.learnkmp.habittrackerandroid.domain.model.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun observeAll(): Flow<List<Habit>>
    fun observeById(habitId: String): Flow<Habit?>
    suspend fun upsert(habit: Habit)
    suspend fun delete(habitId: String)
    suspend fun syncFromFirestore(userId: String)
}
