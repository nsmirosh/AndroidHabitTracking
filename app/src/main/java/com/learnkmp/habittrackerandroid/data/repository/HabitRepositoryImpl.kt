package com.learnkmp.habittrackerandroid.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.learnkmp.habittrackerandroid.data.local.HabitDao
import com.learnkmp.habittrackerandroid.data.local.toDomain
import com.learnkmp.habittrackerandroid.data.local.toEntity
import com.learnkmp.habittrackerandroid.data.remote.FirestoreHabitDataSource
import com.learnkmp.habittrackerandroid.domain.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao,
    private val firestoreSource: FirestoreHabitDataSource,
    private val auth: FirebaseAuth,
) : HabitRepository {

    override fun observeAll(): Flow<List<Habit>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsert(habit: Habit) {
        try {
            val entity = habit.toEntity()
            dao.upsert(entity)
            // Write-through to Firestore; local operation is not affected if this fails
            auth.currentUser?.uid?.let { uid -> firestoreSource.upsert(uid, entity) }
        } catch (e: Exception) {
            Log.e("HabitRepositoryImpl", "Failed to upsert habit: ${e.message}", e)
        }
    }

    override suspend fun delete(habitId: String) {
        dao.deleteById(habitId)
        try {
            auth.currentUser?.uid?.let { uid -> firestoreSource.delete(uid, habitId) }
        } catch (e: Exception) {
            Log.e("HabitRepositoryImpl", "Failed to delete habit from Firestore: ${e.message}", e)
        }
    }

    override suspend fun syncFromFirestore(userId: String) {
        firestoreSource.getAll(userId).forEach { dao.upsert(it) }
    }
}
