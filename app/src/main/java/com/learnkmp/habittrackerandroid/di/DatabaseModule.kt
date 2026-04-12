package com.learnkmp.habittrackerandroid.di

import android.content.Context
import androidx.room.Room
import com.learnkmp.habittrackerandroid.data.local.HabitDao
import com.learnkmp.habittrackerandroid.data.local.HabitDatabase
import com.learnkmp.habittrackerandroid.data.local.HabitProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitDatabase =
        Room.databaseBuilder(context, HabitDatabase::class.java, "habits.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideHabitDao(db: HabitDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideHabitProgressDao(db: HabitDatabase): HabitProgressDao = db.habitProgressDao()
}
