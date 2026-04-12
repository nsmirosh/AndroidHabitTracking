package com.learnkmp.habittrackerandroid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HabitEntity::class, HabitProgressEntity::class],
    version = 5,
    exportSchema = false,
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitProgressDao(): HabitProgressDao
}
