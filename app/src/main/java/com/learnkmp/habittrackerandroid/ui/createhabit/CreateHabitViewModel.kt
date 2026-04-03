package com.learnkmp.habittrackerandroid.ui.createhabit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnkmp.habittrackerandroid.data.repository.HabitRepository
import com.learnkmp.habittrackerandroid.domain.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    var nameInput by mutableStateOf("")
        private set

    fun onNameChange(value: String) {
        nameInput = value
    }

    fun save(onSaved: () -> Unit) {
        val trimmed = nameInput.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            repository.upsert(
                Habit(
                    id = UUID.randomUUID().toString(),
                    name = trimmed,
                    completedToday = false,
                )
            )
            onSaved()
        }
    }
}
