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
                    completedToday = doc.getBoolean("completedToday") ?: false,
                    lastCompletedDate = doc.getString("lastCompletedDate"),
                )
            }

    suspend fun upsert(userId: String, habit: HabitEntity) {
        habitsCollection(userId)
            .document(habit.id)
            .set(
                mapOf(
                    "name" to habit.name,
                    "completedToday" to habit.completedToday,
                    "lastCompletedDate" to habit.lastCompletedDate,
                )
            )
            .await()
    }
}
