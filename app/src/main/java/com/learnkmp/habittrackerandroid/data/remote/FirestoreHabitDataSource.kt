package com.learnkmp.habittrackerandroid.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.learnkmp.habittrackerandroid.data.local.HabitEntity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreHabitDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private fun habitsCollection(userId: String) =
        firestore.collection(COLLECTION_USERS).document(userId).collection(COLLECTION_HABITS)

    suspend fun getAll(userId: String): List<HabitEntity> =
        habitsCollection(userId)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                HabitEntity(
                    id = doc.id,
                    name = doc.getString(FIELD_NAME) ?: return@mapNotNull null,
                    type = doc.getString(FIELD_TYPE) ?: DEFAULT_TYPE,
                    targetCount = doc.getLong(FIELD_TARGET_COUNT)?.toInt() ?: DEFAULT_TARGET_COUNT,
                    progressToday = doc.getLong(FIELD_PROGRESS_TODAY)?.toInt() ?: DEFAULT_PROGRESS_TODAY,
                    lastCompletedDate = doc.getString(FIELD_LAST_COMPLETED_DATE),
                )
            }

    fun upsert(userId: String, habit: HabitEntity) {
        habitsCollection(userId)
            .document(habit.id)
            .set(
                mapOf(
                    FIELD_NAME to habit.name,
                    FIELD_TYPE to habit.type,
                    FIELD_TARGET_COUNT to habit.targetCount,
                    FIELD_PROGRESS_TODAY to habit.progressToday,
                    FIELD_LAST_COMPLETED_DATE to habit.lastCompletedDate,
                )
            )
    }

    fun delete(userId: String, habitId: String) {
        habitsCollection(userId)
            .document(habitId)
            .delete()
    }

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_HABITS = "habits"

        private const val FIELD_NAME = "name"
        private const val FIELD_TYPE = "type"
        private const val FIELD_TARGET_COUNT = "targetCount"
        private const val FIELD_PROGRESS_TODAY = "progressToday"
        private const val FIELD_LAST_COMPLETED_DATE = "lastCompletedDate"

        private const val DEFAULT_TYPE = "TIMES_PER_DAY"
        private const val DEFAULT_TARGET_COUNT = 1
        private const val DEFAULT_PROGRESS_TODAY = 0
    }
}
