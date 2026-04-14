package com.learnkmp.habittrackerandroid.ui.habitlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.learnkmp.habittrackerandroid.domain.model.Habit
import com.learnkmp.habittrackerandroid.domain.model.HabitType
import com.learnkmp.habittrackerandroid.ui.theme.BlackGrey
import com.learnkmp.habittrackerandroid.ui.theme.GreenGradientEnd
import com.learnkmp.habittrackerandroid.ui.theme.GreenGradientStart
import com.learnkmp.habittrackerandroid.ui.theme.HabitGreen
import com.learnkmp.habittrackerandroid.ui.theme.HabitGreenBg
import com.learnkmp.habittrackerandroid.ui.theme.HabitGreyBg
import com.learnkmp.habittrackerandroid.ui.theme.OrangeGradientEnd
import com.learnkmp.habittrackerandroid.ui.theme.OrangeGradientStart
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
            FloatingActionButton(
                onClick = { onCreateHabit(state.selectedDate) },
                containerColor = HabitGreen,
                contentColor = Color.White,
            ) {
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
                HabitListCard(
                    habits = state.habits,
                    onToggleCompleted = { viewModel.onIntent(HabitListIntent.ToggleCompleted(it)) },
                    onDelete = { viewModel.onIntent(HabitListIntent.DeleteHabit(it)) },
                    onClick = { onEditHabit(it.id) },
                )
            }
        }
    }
}

@Composable
private fun HabitListCard(
    habits: List<Habit>,
    onToggleCompleted: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
    onClick: (Habit) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 22.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0x26C8C8C8),
                spotColor = Color(0x26C8C8C8),
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 22.dp),
    ) {
        // Header: "Today Habit" + "See all"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Today Habit",
                style = MaterialTheme.typography.titleLarge,
                color = BlackGrey,
            )
            Text(
                text = "See all",
                style = MaterialTheme.typography.labelMedium.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(OrangeGradientStart, OrangeGradientEnd),
                    ),
                ),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Habit list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(17.dp),
        ) {
            items(habits, key = { it.id }) { habit ->
                HabitItem(
                    habit = habit,
                    onToggleCompleted = { onToggleCompleted(habit) },
                    onDelete = { onDelete(habit) },
                    onClick = { onClick(habit) },
                )
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
    val backgroundColor = if (habit.isCompleted) HabitGreenBg else HabitGreyBg
    val textColor = if (habit.isCompleted) HabitGreen else BlackGrey

    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Habit name
        Text(
            text = habit.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = textColor,
        )

        // Checkbox
        Box(
            modifier = Modifier
                .size(30.dp)
                .clickable(onClick = onToggleCompleted)
                .drawBehind {
                    if (habit.isCompleted) {
                        // Green gradient rounded rect with integrated checkmark (matching Figma SVG)
                        val s = size.width
                        val gradientBrush = Brush.linearGradient(
                            colors = listOf(GreenGradientStart, GreenGradientEnd),
                            start = Offset(0f, s * 0.7f),
                            end = Offset(s * 0.7f, s * 0.7f),
                        )
                        drawRoundRect(
                            brush = gradientBrush,
                            cornerRadius = CornerRadius(s * 0.25f, s * 0.25f),
                        )
                        // White checkmark path (extracted from Figma SVG, normalized to 30x30)
                        val check = Path().apply {
                            val sx = s / 30f
                            val sy = s / 30f
                            moveTo(20.595f * sx, 13.026f * sy)
                            cubicTo(20.73f * sx, 12.882f * sy, 20.835f * sx, 12.713f * sy, 20.904f * sx, 12.529f * sy)
                            cubicTo(20.974f * sx, 12.345f * sy, 21.006f * sx, 12.148f * sy, 21f * sx, 11.951f * sy)
                            cubicTo(20.993f * sx, 11.754f * sy, 20.948f * sx, 11.561f * sy, 20.867f * sx, 11.381f * sy)
                            cubicTo(20.786f * sx, 11.202f * sy, 20.67f * sx, 11.04f * sy, 20.526f * sx, 10.905f * sy)
                            cubicTo(20.382f * sx, 10.77f * sy, 20.213f * sx, 10.665f * sy, 20.029f * sx, 10.596f * sy)
                            cubicTo(19.845f * sx, 10.526f * sy, 19.648f * sx, 10.494f * sy, 19.451f * sx, 10.5f * sy)
                            cubicTo(19.254f * sx, 10.507f * sy, 19.061f * sx, 10.552f * sy, 18.881f * sx, 10.633f * sy)
                            cubicTo(18.702f * sx, 10.714f * sy, 18.54f * sx, 10.83f * sy, 18.405f * sx, 10.974f * sy)
                            lineTo(13.781f * sx, 15.909f * sy)
                            lineTo(11.496f * sx, 13.88f * sy)
                            cubicTo(11.197f * sx, 13.631f * sy, 10.812f * sx, 13.508f * sy, 10.424f * sx, 13.537f * sy)
                            cubicTo(10.036f * sx, 13.567f * sy, 9.675f * sx, 13.746f * sy, 9.416f * sx, 14.036f * sy)
                            cubicTo(9.157f * sx, 14.327f * sy, 9.022f * sx, 14.707f * sy, 9.038f * sx, 15.096f * sy)
                            cubicTo(9.055f * sx, 15.485f * sy, 9.222f * sx, 15.852f * sy, 9.504f * sx, 16.121f * sy)
                            lineTo(12.879f * sx, 19.121f * sy)
                            cubicTo(13.171f * sx, 19.38f * sy, 13.553f * sx, 19.515f * sy, 13.943f * sx, 19.498f * sy)
                            cubicTo(14.333f * sx, 19.48f * sy, 14.701f * sx, 19.311f * sy, 14.969f * sx, 19.026f * sy)
                            lineTo(20.594f * sx, 13.026f * sy)
                            close()
                        }
                        drawPath(check, color = Color.White, style = Fill)
                    } else {
                        // Unchecked: rounded rect outline (matching Figma SVG)
                        val strokeWidth = 2f * (size.width / 30f)
                        val inset = strokeWidth / 2f
                        drawRoundRect(
                            color = BlackGrey,
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            cornerRadius = CornerRadius(size.width * 0.22f, size.width * 0.22f),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                    }
                },
        )

        // Three-dot menu
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = BlackGrey.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp),
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

// region Previews

private val sampleHabits = listOf(
    Habit(id = "1", name = "Meditating", type = HabitType.TIMES_PER_DAY, targetCount = 1, progress = 1),
    Habit(id = "2", name = "Read Philosophy", type = HabitType.MINUTES_PER_DAY, targetCount = 30, progress = 30),
    Habit(id = "3", name = "Journaling", type = HabitType.MINUTES_PER_DAY, targetCount = 30, progress = 0),
)

@Preview(showBackground = true, widthDp = 375, heightDp = 400)
@Composable
private fun HabitListCardPreview() {
    MaterialTheme {
        HabitListCard(
            habits = sampleHabits,
            onToggleCompleted = {},
            onDelete = {},
            onClick = {},
        )
    }
}

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
private fun HabitItemIncompletePreview() {
    MaterialTheme {
        HabitItem(
            habit = sampleHabits[2],
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
