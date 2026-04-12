package com.learnkmp.habittrackerandroid.ui.edithabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnkmp.habittrackerandroid.data.repository.HabitRepository
import com.learnkmp.habittrackerandroid.domain.model.Habit
import com.learnkmp.habittrackerandroid.domain.model.HabitType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class EditHabitState(
    val name: String = "",
    val selectedType: HabitType = HabitType.TIMES_PER_DAY,
    val targetCount: Int = 1,
    val reminderTime: LocalTime? = null,
    val isLoaded: Boolean = false,
)

sealed interface EditHabitIntent {
    data class Load(val id: String) : EditHabitIntent
    data class NameChanged(val value: String) : EditHabitIntent
    data class TypeChanged(val type: HabitType) : EditHabitIntent
    data class TargetCountChanged(val value: Int) : EditHabitIntent
    data class ReminderChanged(val time: LocalTime?) : EditHabitIntent
    data object Save : EditHabitIntent
}

sealed interface EditHabitEffect {
    data object NavigateBack : EditHabitEffect
}

@HiltViewModel
class EditHabitViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditHabitState())
    val state: StateFlow<EditHabitState> = _state

    private val _effect = Channel<EditHabitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var habitId: String = ""
    private var originalHabit: Habit? = null

    fun onIntent(intent: EditHabitIntent) {
        when (intent) {
            is EditHabitIntent.Load -> load(intent.id)
            is EditHabitIntent.NameChanged -> _state.update { it.copy(name = intent.value) }
            is EditHabitIntent.TypeChanged -> _state.update {
                it.copy(
                    selectedType = intent.type,
                    targetCount = if (intent.type == HabitType.MINUTES_PER_DAY) 30 else 1,
                )
            }
            is EditHabitIntent.TargetCountChanged -> {
                if (intent.value >= 1) _state.update { it.copy(targetCount = intent.value) }
            }
            is EditHabitIntent.ReminderChanged -> _state.update { it.copy(reminderTime = intent.time) }
            is EditHabitIntent.Save -> save()
        }
    }

    private fun load(id: String) {
        if (_state.value.isLoaded || habitId == id) return
        habitId = id
        viewModelScope.launch {
            val habit = repository.observeById(id, LocalDate.now()).filterNotNull().first()
            originalHabit = habit
            _state.update {
                it.copy(
                    name = habit.name,
                    selectedType = habit.type,
                    targetCount = habit.targetCount,
                    reminderTime = habit.reminderTime,
                    isLoaded = true,
                )
            }
        }
    }

    private fun save() {
        val trimmed = _state.value.name.trim()
        if (trimmed.isBlank()) return
        val habit = originalHabit ?: return
        viewModelScope.launch {
            repository.upsertHabit(
                habit.copy(
                    name = trimmed,
                    type = _state.value.selectedType,
                    targetCount = _state.value.targetCount,
                    reminderTime = _state.value.reminderTime,
                )
            )
            _effect.send(EditHabitEffect.NavigateBack)
        }
    }
}
