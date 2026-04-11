package com.learnkmp.habittrackerandroid.ui.createhabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnkmp.habittrackerandroid.data.repository.HabitRepository
import com.learnkmp.habittrackerandroid.domain.model.Habit
import com.learnkmp.habittrackerandroid.domain.model.HabitType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreateHabitState(
    val name: String = "",
    val selectedType: HabitType = HabitType.TIMES_PER_DAY,
    val targetCount: Int = 1,
)

sealed interface CreateHabitIntent {
    data class NameChanged(val value: String) : CreateHabitIntent
    data class TypeChanged(val type: HabitType) : CreateHabitIntent
    data class TargetCountChanged(val value: Int) : CreateHabitIntent
    data object Save : CreateHabitIntent
}

sealed interface CreateHabitEffect {
    data object HabitSaved : CreateHabitEffect
}

@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateHabitState())
    val state: StateFlow<CreateHabitState> = _state

    private val _effect = Channel<CreateHabitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: CreateHabitIntent) {
        when (intent) {
            is CreateHabitIntent.NameChanged -> _state.update { it.copy(name = intent.value) }
            is CreateHabitIntent.TypeChanged -> _state.update {
                it.copy(
                    selectedType = intent.type,
                    targetCount = if (intent.type == HabitType.MINUTES_PER_DAY) 30 else 1,
                )
            }
            is CreateHabitIntent.TargetCountChanged -> {
                if (intent.value >= 1) _state.update { it.copy(targetCount = intent.value) }
            }
            is CreateHabitIntent.Save -> save()
        }
    }

    private fun save() {
        val trimmed = _state.value.name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            repository.upsert(
                Habit(
                    id = UUID.randomUUID().toString(),
                    name = trimmed,
                    type = _state.value.selectedType,
                    targetCount = _state.value.targetCount,
                )
            )
            _effect.send(CreateHabitEffect.HabitSaved)
        }
    }
}
