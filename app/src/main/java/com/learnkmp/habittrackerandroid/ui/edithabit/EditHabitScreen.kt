package com.learnkmp.habittrackerandroid.ui.edithabit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.learnkmp.habittrackerandroid.domain.model.HabitType
import com.learnkmp.habittrackerandroid.ui.common.ReminderPickerSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    habitId: String,
    onBack: () -> Unit,
    viewModel: EditHabitViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(habitId) { viewModel.onIntent(EditHabitIntent.Load(habitId)) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EditHabitEffect.NavigateBack -> onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Habit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.onIntent(EditHabitIntent.Save) },
                        enabled = state.name.isNotBlank(),
                    ) {
                        Text("Save")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (!state.isLoaded) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onIntent(EditHabitIntent.NameChanged(it)) },
                label = { Text("Habit name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            Spacer(Modifier.height(16.dp))

            Text("Tracking type", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = state.selectedType == HabitType.TIMES_PER_DAY,
                    onClick = { viewModel.onIntent(EditHabitIntent.TypeChanged(HabitType.TIMES_PER_DAY)) },
                    label = { Text("Times") },
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = state.selectedType == HabitType.MINUTES_PER_DAY,
                    onClick = { viewModel.onIntent(EditHabitIntent.TypeChanged(HabitType.MINUTES_PER_DAY)) },
                    label = { Text("Minutes") },
                )
                Spacer(Modifier.width(8.dp))
                Text("per day", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.targetCount.toString(),
                onValueChange = { it.toIntOrNull()?.let { v -> viewModel.onIntent(EditHabitIntent.TargetCountChanged(v)) } },
                label = {
                    Text(
                        if (state.selectedType == HabitType.TIMES_PER_DAY) "Target (times)"
                        else "Target (minutes)"
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.onIntent(EditHabitIntent.Save) },
                ),
            )

            Spacer(Modifier.height(16.dp))

            Text("Reminder", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            ReminderPickerSection(
                reminderTime = state.reminderTime,
                onChange = { viewModel.onIntent(EditHabitIntent.ReminderChanged(it)) },
            )
        }
    }
}
