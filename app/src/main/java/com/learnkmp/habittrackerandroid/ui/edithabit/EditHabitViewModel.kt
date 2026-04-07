package com.learnkmp.habittrackerandroid.ui.edithabit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnkmp.habittrackerandroid.data.repository.HabitRepository
import com.learnkmp.habittrackerandroid.domain.model.Habit
import com.learnkmp.habittrackerandroid.domain.model.HabitType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditHabitViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    private var habitId: String = ""

    var nameInput by mutableStateOf("")
        private set

    var selectedType by mutableStateOf(HabitType.TIMES_PER_DAY)
        private set

    var targetCount by mutableIntStateOf(1)
        private set

    var isLoaded by mutableStateOf(false)
        private set

    private var originalHabit: Habit? = null

    fun load(id: String) {
        if (isLoaded || habitId == id) return
        habitId = id
        viewModelScope.launch {
            val habit = repository.observeById(id).filterNotNull().first()
            originalHabit = habit
            nameInput = habit.name
            selectedType = habit.type
            targetCount = habit.targetCount
            isLoaded = true
        }
    }

    fun onNameChange(value: String) {
        nameInput = value
    }

    fun onTypeChange(type: HabitType) {
        selectedType = type
        targetCount = if (type == HabitType.MINUTES_PER_DAY) 30 else 1
    }

    fun onTargetCountChange(value: Int) {
        if (value >= 1) targetCount = value
    }

    fun save(onSaved: () -> Unit) {
        val trimmed = nameInput.trim()
        if (trimmed.isBlank()) return
        val habit = originalHabit ?: return
        viewModelScope.launch {
            repository.upsert(
                habit.copy(
                    name = trimmed,
                    type = selectedType,
                    targetCount = targetCount,
                )
            )
            onSaved()
        }
    }

    fun resetForToday(onDone: () -> Unit) {
        val habit = originalHabit ?: return
        viewModelScope.launch {
            repository.upsert(habit.copy(progressToday = 0))
            onDone()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.delete(habitId)
            onDeleted()
        }
    }
}
