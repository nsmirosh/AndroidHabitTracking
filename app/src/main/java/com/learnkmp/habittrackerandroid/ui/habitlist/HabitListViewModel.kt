package com.learnkmp.habittrackerandroid.ui.habitlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnkmp.habittrackerandroid.data.repository.HabitRepository
import com.learnkmp.habittrackerandroid.domain.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HabitListState(
    val habits: List<Habit> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val canGoForward: Boolean = false,
)

sealed interface HabitListIntent {
    data class ToggleCompleted(val habit: Habit) : HabitListIntent
    data class DeleteHabit(val habit: Habit) : HabitListIntent
    data object PreviousDay : HabitListIntent
    data object NextDay : HabitListIntent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())

    val state: StateFlow<HabitListState> = combine(
        selectedDate,
        selectedDate.flatMapLatest { repository.observeAll(it) },
    ) { date, habits ->
        HabitListState(
            habits = habits,
            selectedDate = date,
            canGoForward = date.isBefore(LocalDate.now()),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitListState())

    fun onIntent(intent: HabitListIntent) {
        when (intent) {
            is HabitListIntent.ToggleCompleted -> toggleCompleted(intent.habit)
            is HabitListIntent.DeleteHabit -> deleteHabit(intent.habit)
            HabitListIntent.PreviousDay -> selectedDate.value = selectedDate.value.minusDays(1)
            HabitListIntent.NextDay -> {
                val next = selectedDate.value.plusDays(1)
                if (!next.isAfter(LocalDate.now())) selectedDate.value = next
            }
        }
    }

    private fun toggleCompleted(habit: Habit) {
        viewModelScope.launch {
            val newProgress = if (habit.isCompleted) 0 else habit.progress + 1
            repository.setProgress(habit.id, selectedDate.value, newProgress)
        }
    }

    private fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.delete(habit.id)
        }
    }
}
