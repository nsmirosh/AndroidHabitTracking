package com.learnkmp.habittrackerandroid.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitProgressDao {
    @Query("SELECT * FROM habit_progress WHERE date = :date")
    fun observeForDate(date: String): Flow<List<HabitProgressEntity>>

    @Query("SELECT * FROM habit_progress WHERE habitId = :habitId AND date = :date LIMIT 1")
    fun observeForHabitAndDate(habitId: String, date: String): Flow<HabitProgressEntity?>

    @Upsert
    suspend fun upsert(entry: HabitProgressEntity)

    @Query("DELETE FROM habit_progress WHERE habitId = :habitId AND date = :date")
    suspend fun delete(habitId: String, date: String)

    @Query("DELETE FROM habit_progress WHERE habitId = :habitId")
    suspend fun deleteAllForHabit(habitId: String)
}
