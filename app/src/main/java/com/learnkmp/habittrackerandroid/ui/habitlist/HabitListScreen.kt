package com.learnkmp.habittrackerandroid.ui.habitlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.learnkmp.habittrackerandroid.domain.model.Habit
import com.learnkmp.habittrackerandroid.domain.model.HabitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onCreateHabit: () -> Unit,
    onEditHabit: (String) -> Unit,
    viewModel: HabitListViewModel = hiltViewModel(),
) {
    val habits by viewModel.habits.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Habits") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateHabit) {
                Icon(Icons.Default.Add, contentDescription = "Add habit")
            }
        },
    ) { innerPadding ->
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(habits, key = { it.id }) { habit ->
                    HabitItem(
                        habit = habit,
                        onToggleCompleted = { viewModel.toggleCompleted(habit) },
                        onDelete = { viewModel.deleteHabit(habit) },
                        onClick = { onEditHabit(habit.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitItem(
    habit: Habit,
    onToggleCompleted: () -> Unit = {},
    onDelete: () -> Unit = {},
    onClick: () -> Unit,
) {
    val containerColor = if (habit.completedToday) {
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
                checked = habit.completedToday,
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
                    text = "${habit.progressToday} / ${habit.targetCount} $label",
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
                    if (habit.completedToday) {
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
    Habit(id = "1", name = "Drink Water", type = HabitType.TIMES_PER_DAY, targetCount = 8, progressToday = 5),
    Habit(id = "2", name = "Meditate", type = HabitType.MINUTES_PER_DAY, targetCount = 15, progressToday = 15),
    Habit(id = "3", name = "Read", type = HabitType.MINUTES_PER_DAY, targetCount = 30, progressToday = 0),
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
private fun HabitItemWithMenuPreview() {
    MaterialTheme {
        HabitItem(
            habit = sampleHabits[0],
            onToggleCompleted = {},
            onDelete = {},
            onClick = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HabitListScreenEmptyPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(title = { Text("My Habits") })
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = "Add habit")
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No habits yet. Tap + to add one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HabitListScreenWithHabitsPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(title = { Text("My Habits") })
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = "Add habit")
                }
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(sampleHabits, key = { it.id }) { habit ->
                    HabitItem(habit = habit, onClick = {})
                }
            }
        }
    }
}

// endregion
