package com.learnkmp.habittrackerandroid.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.learnkmp.habittrackerandroid.data.local.HabitEntity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreHabitDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private fun habitsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("habits")

    suspend fun getAll(userId: String): List<HabitEntity> =
        habitsCollection(userId)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                HabitEntity(
                    id = doc.id,
                    name = doc.getString("name") ?: return@mapNotNull null,
                    type = doc.getString("type") ?: "TIMES_PER_DAY",
                    targetCount = doc.getLong("targetCount")?.toInt() ?: 1,
                    progressToday = doc.getLong("progressToday")?.toInt() ?: 0,
                    lastCompletedDate = doc.getString("lastCompletedDate"),
                )
            }

    fun upsert(userId: String, habit: HabitEntity) {
        habitsCollection(userId)
            .document(habit.id)
            .set(
                mapOf(
                    "name" to habit.name,
                    "type" to habit.type,
                    "targetCount" to habit.targetCount,
                    "progressToday" to habit.progressToday,
                    "lastCompletedDate" to habit.lastCompletedDate,
                )
            )
    }

    fun delete(userId: String, habitId: String) {
        habitsCollection(userId)
            .document(habitId)
            .delete()
    }
}
