package com.learnkmp.habittrackerandroid.ui.habitlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.learnkmp.habittrackerandroid.domain.model.Habit
import com.learnkmp.habittrackerandroid.domain.model.HabitType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

private val DATE_LABEL_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
private const val SWIPE_THRESHOLD_PX = 120f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onCreateHabit: (LocalDate) -> Unit,
    onEditHabit: (String) -> Unit,
    viewModel: HabitListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    DateNavigator(
                        date = state.selectedDate,
                        canGoForward = state.canGoForward,
                        onPrev = { viewModel.onIntent(HabitListIntent.PreviousDay) },
                        onNext = { viewModel.onIntent(HabitListIntent.NextDay) },
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onCreateHabit(state.selectedDate) }) {
                Icon(Icons.Default.Add, contentDescription = "Add habit")
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(state.canGoForward) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onDragEnd = {
                            if (abs(totalDrag) >= SWIPE_THRESHOLD_PX) {
                                if (totalDrag > 0) {
                                    viewModel.onIntent(HabitListIntent.PreviousDay)
                                } else {
                                    viewModel.onIntent(HabitListIntent.NextDay)
                                }
                            }
                        },
                        onDragCancel = { totalDrag = 0f },
                    ) { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                },
        ) {
            if (state.habits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No habits yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.habits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            onToggleCompleted = { viewModel.onIntent(HabitListIntent.ToggleCompleted(habit)) },
                            onDelete = { viewModel.onIntent(HabitListIntent.DeleteHabit(habit)) },
                            onClick = { onEditHabit(habit.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateNavigator(
    date: LocalDate,
    canGoForward: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous day",
            )
        }
        Text(
            text = dateLabel(date),
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = onNext, enabled = canGoForward) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next day",
            )
        }
    }
}

private fun dateLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DATE_LABEL_FORMAT)
    }
}

@Composable
private fun HabitItem(
    habit: Habit,
    onToggleCompleted: () -> Unit = {},
    onDelete: () -> Unit = {},
    onClick: () -> Unit,
) {
    val containerColor = if (habit.isCompleted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor)
                .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = habit.isCompleted,
                onCheckedChange = { onToggleCompleted() },
            )
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.bodyLarge,
                )
                val label = if (habit.type == HabitType.TIMES_PER_DAY) "times" else "min"
                Text(
                    text = "${habit.progress} / ${habit.targetCount} $label",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    if (habit.isCompleted) {
                        DropdownMenuItem(
                            text = { Text("Restart") },
                            onClick = {
                                menuExpanded = false
                                onToggleCompleted()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        },
                    )
                }
            }
        }
    }
}

// region Previews

private val sampleHabits = listOf(
    Habit(id = "1", name = "Drink Water", type = HabitType.TIMES_PER_DAY, targetCount = 8, progress = 5),
    Habit(id = "2", name = "Meditate", type = HabitType.MINUTES_PER_DAY, targetCount = 15, progress = 15),
    Habit(id = "3", name = "Read", type = HabitType.MINUTES_PER_DAY, targetCount = 30, progress = 0),
)

@Preview(showBackground = true)
@Composable
private fun HabitItemPreview() {
    MaterialTheme {
        HabitItem(
            habit = sampleHabits[0],
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HabitItemCompletedPreview() {
    MaterialTheme {
        HabitItem(
            habit = sampleHabits[1],
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DateNavigatorPreview() {
    MaterialTheme {
        DateNavigator(
            date = LocalDate.now().minusDays(1),
            canGoForward = true,
            onPrev = {},
            onNext = {},
        )
    }
}
