package com.learnkmp.habittrackerandroid.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE createdDate <= :date ORDER BY name ASC")
    fun observeAllCreatedOnOrBefore(date: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<HabitEntity?>

    @Upsert
    suspend fun upsert(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: String)
}
