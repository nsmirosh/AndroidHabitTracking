package com.learnkmp.habittrackerandroid.ui.edithabit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.learnkmp.habittrackerandroid.domain.model.HabitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    habitId: String,
    onBack: () -> Unit,
    viewModel: EditHabitViewModel = hiltViewModel(),
) {
    LaunchedEffect(habitId) { viewModel.load(habitId) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Habit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (!viewModel.isLoaded) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            OutlinedTextField(
                value = viewModel.nameInput,
                onValueChange = viewModel::onNameChange,
                label = { Text("Habit name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            Spacer(Modifier.height(16.dp))

            Text("Tracking type", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row {
                FilterChip(
                    selected = viewModel.selectedType == HabitType.TIMES_PER_DAY,
                    onClick = { viewModel.onTypeChange(HabitType.TIMES_PER_DAY) },
                    label = { Text("Times per day") },
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = viewModel.selectedType == HabitType.MINUTES_PER_DAY,
                    onClick = { viewModel.onTypeChange(HabitType.MINUTES_PER_DAY) },
                    label = { Text("Minutes per day") },
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.targetCount.toString(),
                onValueChange = { it.toIntOrNull()?.let(viewModel::onTargetCountChange) },
                label = {
                    Text(
                        if (viewModel.selectedType == HabitType.TIMES_PER_DAY) "Target (times)"
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
                    onDone = { viewModel.save(onBack) },
                ),
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.save(onBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.nameInput.isNotBlank(),
            ) {
                Text("Save Changes")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.resetForToday(onBack) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Reset Progress for Today")
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { viewModel.delete(onBack) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Delete Habit")
            }
        }
    }
}
