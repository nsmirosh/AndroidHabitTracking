package com.learnkmp.habittrackerandroid.ui.habitlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnkmp.habittrackerandroid.data.repository.HabitRepository
import com.learnkmp.habittrackerandroid.domain.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitListState(
    val habits: List<Habit> = emptyList(),
)

sealed interface HabitListIntent {
    data class ToggleCompleted(val habit: Habit) : HabitListIntent
    data class DeleteHabit(val habit: Habit) : HabitListIntent
}

@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    val state: StateFlow<HabitListState> = repository.observeAll()
        .map { HabitListState(habits = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitListState())

    fun onIntent(intent: HabitListIntent) {
        when (intent) {
            is HabitListIntent.ToggleCompleted -> toggleCompleted(intent.habit)
            is HabitListIntent.DeleteHabit -> deleteHabit(intent.habit)
        }
    }

    private fun toggleCompleted(habit: Habit) {
        viewModelScope.launch {
            val updated = if (habit.completedToday) {
                habit.copy(progressToday = 0)
            } else {
                habit.copy(progressToday = habit.progressToday + 1)
            }
            repository.upsert(updated)
        }
    }

    private fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.delete(habit.id)
        }
    }
}
