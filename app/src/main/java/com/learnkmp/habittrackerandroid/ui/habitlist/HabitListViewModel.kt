package com.learnkmp.habittrackerandroid.ui.habitlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnkmp.habittrackerandroid.data.repository.HabitRepository
import com.learnkmp.habittrackerandroid.domain.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    val habits: StateFlow<List<Habit>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleCompleted(habit: Habit) {
        viewModelScope.launch {
            val updated = if (habit.completedToday) {
                habit.copy(progressToday = 0)
            } else {
                habit.copy(progressToday = habit.progressToday + 1)
            }
            repository.upsert(updated)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.delete(habit.id)
        }
    }
}
