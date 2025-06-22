package com.nagopy.kmp.habittracker.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.presentation.ui.parseColor
import habittracker.composeapp.generated.resources.Res
import habittracker.composeapp.generated.resources.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Compose screen for displaying today's tasks.
 * Shows all scheduled task instances for the current day with their scheduled times.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    TodayScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onTaskComplete = viewModel::completeTask
    )
}

/**
 * Stateless content for TodayScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayScreenContent(
    uiState: TodayUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onTaskComplete: (Task) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.todays_tasks)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is TodayUiState.Loading -> LoadingState()
                is TodayUiState.Error -> ErrorState(
                    error = uiState.message,
                    onRetry = onRefresh
                )
                is TodayUiState.Empty -> EmptyState()
                is TodayUiState.Content -> TasksList(
                    tasks = uiState.tasks,
                    onTaskComplete = onTaskComplete
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.error_prefix, error),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text(stringResource(Res.string.retry))
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.no_tasks_scheduled),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TasksList(
    tasks: List<Task>,
    onTaskComplete: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tasks) { task ->
            TaskItem(
                task = task,
                onTaskComplete = onTaskComplete
            )
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onTaskComplete: (Task) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time indicator
            TimeIndicator(time = task.scheduledTime)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Habit color indicator
            HabitColorIndicator(color = task.habitColor)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Task details
            TaskDetails(
                habitName = task.habitName,
                habitDescription = task.habitDescription,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Completion checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { if (!task.isCompleted) onTaskComplete(task) },
                enabled = !task.isCompleted
            )
        }
    }
}

@Composable
private fun TimeIndicator(time: LocalTime) {
    Text(
        text = formatTime(time),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.width(80.dp)
    )
}

@Composable
private fun HabitColorIndicator(color: String) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(parseColor(color))
    )
}

@Composable
private fun TaskDetails(
    habitName: String,
    habitDescription: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = habitName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        if (habitDescription.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = habitDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatTime(time: LocalTime): String {
    return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

// ========== Preview Helper Functions ==========

// ========== Screen-Level Previews ==========

@Preview
@Composable
private fun TodayScreenWithTasksPreview() {
    val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val sampleTasks = listOf(
        Task(
            habitId = 1L,
            habitName = "Drink Water",
            habitDescription = "Stay hydrated throughout the day",
            habitColor = "#2196F3",
            date = currentDate,
            scheduledTime = LocalTime(9, 0),
            isCompleted = false
        ),
        Task(
            habitId = 2L,
            habitName = "Exercise",
            habitDescription = "Daily workout routine for health",
            habitColor = "#4CAF50",
            date = currentDate,
            scheduledTime = LocalTime(7, 30),
            isCompleted = true
        ),
        Task(
            habitId = 3L,
            habitName = "Meditation",
            habitDescription = "",
            habitColor = "#9C27B0",
            date = currentDate,
            scheduledTime = LocalTime(18, 0),
            isCompleted = false
        ),
        Task(
            habitId = 1L,
            habitName = "Drink Water",
            habitDescription = "Stay hydrated throughout the day",
            habitColor = "#2196F3",
            date = currentDate,
            scheduledTime = LocalTime(14, 0),
            isCompleted = false
        )
    )
    
    MaterialTheme {
        TodayScreenContent(
            uiState = TodayUiState.Content(
                tasks = sampleTasks
            ),
            onNavigateBack = {},
            onRefresh = {},
            onTaskComplete = {}
        )
    }
}

@Preview
@Composable
private fun TodayScreenEmptyStatePreview() {
    MaterialTheme {
        TodayScreenContent(
            uiState = TodayUiState.Empty,
            onNavigateBack = {},
            onRefresh = {},
            onTaskComplete = {}
        )
    }
}

@Preview
@Composable
private fun TodayScreenLoadingStatePreview() {
    MaterialTheme {
        TodayScreenContent(
            uiState = TodayUiState.Loading,
            onNavigateBack = {},
            onRefresh = {},
            onTaskComplete = {}
        )
    }
}

@Preview
@Composable
private fun TodayScreenErrorStatePreview() {
    MaterialTheme {
        TodayScreenContent(
            uiState = TodayUiState.Error(
                message = "Failed to load today's tasks"
            ),
            onNavigateBack = {},
            onRefresh = {},
            onTaskComplete = {}
        )
    }
}

// ========== Individual Component Previews ==========

@Preview
@Composable
private fun TaskItemPreview() {
    val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val sampleTask = Task(
        habitId = 1L,
        habitName = "Drink Water",
        habitDescription = "Stay hydrated throughout the day",
        habitColor = "#2196F3",
        date = currentDate,
        scheduledTime = LocalTime(9, 0),
        isCompleted = false
    )
    
    MaterialTheme {
        Surface {
            TaskItem(
                task = sampleTask,
                onTaskComplete = {}
            )
        }
    }
}

@Preview
@Composable
private fun TaskItemCompletedPreview() {
    val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val sampleTask = Task(
        habitId = 2L,
        habitName = "Exercise",
        habitDescription = "Daily workout routine for health",
        habitColor = "#4CAF50",
        date = currentDate,
        scheduledTime = LocalTime(7, 30),
        isCompleted = true
    )
    
    MaterialTheme {
        Surface {
            TaskItem(
                task = sampleTask,
                onTaskComplete = {}
            )
        }
    }
}

@Preview
@Composable
private fun TaskItemNoDescriptionPreview() {
    val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val sampleTask = Task(
        habitId = 3L,
        habitName = "Meditation",
        habitDescription = "",
        habitColor = "#9C27B0",
        date = currentDate,
        scheduledTime = LocalTime(18, 0),
        isCompleted = false
    )
    
    MaterialTheme {
        Surface {
            TaskItem(
                task = sampleTask,
                onTaskComplete = {}
            )
        }
    }
}