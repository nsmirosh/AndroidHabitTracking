package com.learnkmp.habittrackerandroid.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.learnkmp.habittrackerandroid.data.local.HabitEntity
import com.learnkmp.habittrackerandroid.data.local.HabitProgressEntity
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject

class FirestoreHabitDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private fun habitsCollection(userId: String) =
        firestore.collection(COLLECTION_USERS).document(userId).collection(COLLECTION_HABITS)

    private fun progressCollection(userId: String, habitId: String) =
        habitsCollection(userId).document(habitId).collection(COLLECTION_PROGRESS)

    suspend fun getAllHabits(userId: String): List<HabitEntity> =
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
                    createdDate = doc.getString(FIELD_CREATED_DATE) ?: LocalDate.now().toString(),
                    reminderTime = doc.getString(FIELD_REMINDER_TIME),
                )
            }

    suspend fun getProgressForHabit(userId: String, habitId: String): List<HabitProgressEntity> =
        progressCollection(userId, habitId)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                val progress = doc.getLong(FIELD_PROGRESS)?.toInt() ?: return@mapNotNull null
                HabitProgressEntity(habitId = habitId, date = doc.id, progress = progress)
            }

    fun upsertHabit(userId: String, habit: HabitEntity) {
        habitsCollection(userId)
            .document(habit.id)
            .set(
                mapOf(
                    FIELD_NAME to habit.name,
                    FIELD_TYPE to habit.type,
                    FIELD_TARGET_COUNT to habit.targetCount,
                    FIELD_CREATED_DATE to habit.createdDate,
                    FIELD_REMINDER_TIME to habit.reminderTime,
                )
            )
    }

    fun deleteHabit(userId: String, habitId: String) {
        habitsCollection(userId)
            .document(habitId)
            .delete()
    }

    fun upsertProgress(userId: String, entry: HabitProgressEntity) {
        progressCollection(userId, entry.habitId)
            .document(entry.date)
            .set(mapOf(FIELD_PROGRESS to entry.progress))
    }

    fun deleteProgress(userId: String, habitId: String, date: String) {
        progressCollection(userId, habitId)
            .document(date)
            .delete()
    }

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_HABITS = "habits"
        private const val COLLECTION_PROGRESS = "progress"

        private const val FIELD_NAME = "name"
        private const val FIELD_TYPE = "type"
        private const val FIELD_TARGET_COUNT = "targetCount"
        private const val FIELD_CREATED_DATE = "createdDate"
        private const val FIELD_REMINDER_TIME = "reminderTime"
        private const val FIELD_PROGRESS = "progress"

        private const val DEFAULT_TYPE = "TIMES_PER_DAY"
        private const val DEFAULT_TARGET_COUNT = 1
    }
}
