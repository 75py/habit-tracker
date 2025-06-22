package com.nagopy.kmp.habittracker.presentation.habitlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitDetail
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
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
 * Compose screen for displaying the list of habits.
 * Shows all habits with their name, description, and color indicator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onAddHabitClick: () -> Unit,
    onTodayClick: () -> Unit,
    onHabitEdit: (Habit) -> Unit,
    viewModel: HabitListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    HabitListContent(
        uiState = uiState,
        onAddHabitClick = onAddHabitClick,
        onTodayClick = onTodayClick,
        onHabitEdit = onHabitEdit,
        onHabitDelete = viewModel::deleteHabit,
        onRefresh = viewModel::refresh
    )
}

/**
 * Stateless content for HabitListScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitListContent(
    uiState: HabitListUiState,
    onAddHabitClick: () -> Unit,
    onTodayClick: () -> Unit,
    onHabitEdit: (Habit) -> Unit,
    onHabitDelete: (Long) -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.my_habits)) },
                actions = {
                    IconButton(onClick = onTodayClick) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(Res.string.todays_tasks_content_description)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabitClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.add_habit)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is HabitListUiState.Loading -> LoadingState()
                is HabitListUiState.Error -> ErrorState(
                    error = uiState.message,
                    onRetry = onRefresh
                )
                is HabitListUiState.Empty -> EmptyState()
                is HabitListUiState.Content -> HabitsList(
                    habits = uiState.habits,
                    onHabitEdit = onHabitEdit,
                    onHabitDelete = onHabitDelete
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
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.no_habits_yet),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.add_first_habit),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HabitsList(
    habits: List<Habit>,
    onHabitEdit: (Habit) -> Unit,
    onHabitDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(habits) { habit ->
            HabitItem(
                habit = habit,
                onEdit = { onHabitEdit(habit) },
                onDelete = { onHabitDelete(habit.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitItem(
    habit: Habit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            HabitColorIndicator(color = habit.color)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Habit details
            HabitDetails(
                name = habit.name,
                description = habit.description,
                isActive = habit.isActive,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Action buttons
            HabitActions(
                onEdit = onEdit,
                onDelete = { showDeleteConfirmation = true }
            )
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            habitName = habit.name,
            onConfirm = {
                onDelete()
                showDeleteConfirmation = false
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

@Composable
private fun HabitColorIndicator(color: String) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(parseColor(color))
    )
}

@Composable
private fun HabitDetails(
    name: String,
    description: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        if (!isActive) {
            Spacer(modifier = Modifier.width(8.dp))
            InactiveLabel()
        }
    }
}

@Composable
private fun InactiveLabel() {
    Text(
        text = stringResource(Res.string.inactive),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline
    )
}

@Composable
private fun HabitActions(
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row {
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(Res.string.edit_habit_content_description),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(Res.string.delete_habit_content_description),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    habitName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.delete_habit)) },
        text = { Text(stringResource(Res.string.delete_habit_confirmation, habitName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}

// ========== Screen-Level Previews ==========

@Preview
@Composable
private fun HabitListScreenWithHabitsPreview() {
    val sampleHabits = listOf(
        Habit(
            id = 1L,
            name = "Drink Water",
            description = "Stay hydrated throughout the day",
            color = "#2196F3",
            isActive = true,
            createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            detail = HabitDetail.OnceDailyHabitDetail(scheduledTime = LocalTime(9, 0))
        ),
        Habit(
            id = 2L,
            name = "Exercise",
            description = "Daily workout routine for health",
            color = "#4CAF50",
            isActive = true,
            createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            detail = HabitDetail.OnceDailyHabitDetail(scheduledTime = LocalTime(7, 30))
        ),
        Habit(
            id = 3L,
            name = "Meditation",
            description = "",
            color = "#9C27B0",
            isActive = false,
            createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            detail = HabitDetail.OnceDailyHabitDetail(scheduledTime = LocalTime(18, 0))
        )
    )
    
    MaterialTheme {
        HabitListContent(
            uiState = HabitListUiState.Content(
                habits = sampleHabits
            ),
            onAddHabitClick = {},
            onTodayClick = {},
            onHabitEdit = {},
            onHabitDelete = {},
            onRefresh = {}
        )
    }
}

@Preview
@Composable
private fun HabitListScreenEmptyStatePreview() {
    MaterialTheme {
        HabitListContent(
            uiState = HabitListUiState.Empty,
            onAddHabitClick = {},
            onTodayClick = {},
            onHabitEdit = {},
            onHabitDelete = {},
            onRefresh = {}
        )
    }
}

@Preview
@Composable
private fun HabitListScreenLoadingStatePreview() {
    MaterialTheme {
        HabitListContent(
            uiState = HabitListUiState.Loading,
            onAddHabitClick = {},
            onTodayClick = {},
            onHabitEdit = {},
            onHabitDelete = {},
            onRefresh = {}
        )
    }
}

@Preview
@Composable
private fun HabitListScreenErrorStatePreview() {
    MaterialTheme {
        HabitListContent(
            uiState = HabitListUiState.Error(
                message = "Network connection failed"
            ),
            onAddHabitClick = {},
            onTodayClick = {},
            onHabitEdit = {},
            onHabitDelete = {},
            onRefresh = {}
        )
    }
}

// ========== Individual Component Previews ==========

@Preview
@Composable
private fun HabitItemPreview() {
    val sampleHabit = Habit(
        id = 1L,
        name = "Drink Water",
        description = "Stay hydrated throughout the day",
        color = "#2196F3",
        isActive = true,
        createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        detail = HabitDetail.OnceDailyHabitDetail(scheduledTime = LocalTime(9, 0))
    )
    
    MaterialTheme {
        Surface {
            HabitItem(
                habit = sampleHabit,
                onEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview
@Composable
private fun HabitItemInactivePreview() {
    val sampleHabit = Habit(
        id = 2L,
        name = "Exercise",
        description = "Daily workout routine for health",
        color = "#4CAF50",
        isActive = false,
        createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        detail = HabitDetail.OnceDailyHabitDetail(scheduledTime = LocalTime(7, 30))
    )
    
    MaterialTheme {
        Surface {
            HabitItem(
                habit = sampleHabit,
                onEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview
@Composable
private fun HabitItemNoDescriptionPreview() {
    val sampleHabit = Habit(
        id = 3L,
        name = "Meditation",
        description = "",
        color = "#9C27B0",
        isActive = true,
        createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        detail = HabitDetail.OnceDailyHabitDetail(scheduledTime = LocalTime(18, 0))
    )
    
    MaterialTheme {
        Surface {
            HabitItem(
                habit = sampleHabit,
                onEdit = {},
                onDelete = {}
            )
        }
    }
}