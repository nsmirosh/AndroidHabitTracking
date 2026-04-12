package com.learnkmp.habittrackerandroid.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.learnkmp.habittrackerandroid.data.local.HabitDao
import com.learnkmp.habittrackerandroid.data.local.HabitProgressDao
import com.learnkmp.habittrackerandroid.data.local.HabitProgressEntity
import com.learnkmp.habittrackerandroid.data.local.toDomain
import com.learnkmp.habittrackerandroid.data.local.toEntity
import com.learnkmp.habittrackerandroid.data.remote.FirestoreHabitDataSource
import com.learnkmp.habittrackerandroid.domain.model.Habit
import com.learnkmp.habittrackerandroid.reminders.HabitReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao,
    private val progressDao: HabitProgressDao,
    private val firestoreSource: FirestoreHabitDataSource,
    private val auth: FirebaseAuth,
    private val reminderScheduler: HabitReminderScheduler,
) : HabitRepository {

    override fun observeAll(date: LocalDate): Flow<List<Habit>> {
        val dateKey = date.toString()
        return combine(
            dao.observeAllCreatedOnOrBefore(dateKey),
            progressDao.observeForDate(dateKey),
        ) { habits, progress ->
            val progressByHabit = progress.associateBy { it.habitId }
            habits.map { it.toDomain(progressByHabit[it.id]?.progress ?: 0) }
        }
    }

    override fun observeById(habitId: String, date: LocalDate): Flow<Habit?> =
        combine(
            dao.observeById(habitId),
            progressDao.observeForHabitAndDate(habitId, date.toString()),
        ) { entity, progress ->
            entity?.toDomain(progress?.progress ?: 0)
        }

    override suspend fun upsertHabit(habit: Habit) {
        try {
            val entity = habit.toEntity()
            dao.upsert(entity)
            if (habit.reminderTime != null) {
                reminderScheduler.schedule(habit)
            } else {
                reminderScheduler.cancel(habit.id)
            }
            auth.currentUser?.uid?.let { uid -> firestoreSource.upsertHabit(uid, entity) }
        } catch (e: Exception) {
            Log.e("HabitRepositoryImpl", "Failed to upsert habit: ${e.message}", e)
        }
    }

    override suspend fun setProgress(habitId: String, date: LocalDate, progress: Int) {
        try {
            val dateKey = date.toString()
            if (progress <= 0) {
                progressDao.delete(habitId, dateKey)
                auth.currentUser?.uid?.let { uid ->
                    firestoreSource.deleteProgress(uid, habitId, dateKey)
                }
            } else {
                val entry = HabitProgressEntity(habitId = habitId, date = dateKey, progress = progress)
                progressDao.upsert(entry)
                auth.currentUser?.uid?.let { uid ->
                    firestoreSource.upsertProgress(uid, entry)
                }
            }
        } catch (e: Exception) {
            Log.e("HabitRepositoryImpl", "Failed to set progress: ${e.message}", e)
        }
    }

    override suspend fun delete(habitId: String) {
        dao.deleteById(habitId)
        progressDao.deleteAllForHabit(habitId)
        reminderScheduler.cancel(habitId)
        try {
            auth.currentUser?.uid?.let { uid -> firestoreSource.deleteHabit(uid, habitId) }
        } catch (e: Exception) {
            Log.e("HabitRepositoryImpl", "Failed to delete habit from Firestore: ${e.message}", e)
        }
    }

    override suspend fun syncFromFirestore(userId: String) {
        val habits = firestoreSource.getAllHabits(userId)
        habits.forEach { entity ->
            dao.upsert(entity)
            val domain = entity.toDomain()
            if (domain.reminderTime != null) reminderScheduler.schedule(domain)
        }
        habits.forEach { habit ->
            firestoreSource.getProgressForHabit(userId, habit.id).forEach { progressDao.upsert(it) }
        }
    }
}
