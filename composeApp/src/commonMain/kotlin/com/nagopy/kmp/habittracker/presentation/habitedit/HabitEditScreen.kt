package com.nagopy.kmp.habittracker.presentation.habitedit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.presentation.habitedit.components.*
import habittracker.composeapp.generated.resources.Res
import habittracker.composeapp.generated.resources.*
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Compose screen for adding or editing a habit.
 * Provides form fields for name, description, color selection, and active status.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditScreen(
    habitId: Long? = null,
    onSaveSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: HabitEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Load habit for editing if habitId is provided
    LaunchedEffect(habitId) {
        if (habitId != null) {
            viewModel.loadHabitForEdit(habitId)
        }
    }
    
    HabitEditContent(
        uiState = uiState,
        onNameChange = viewModel::updateName,
        onDescriptionChange = viewModel::updateDescription,
        onColorChange = viewModel::updateColor,
        onFrequencyTypeChange = viewModel::updateFrequencyType,
        onIntervalValueChange = viewModel::updateIntervalValue,
        onIntervalUnitChange = viewModel::updateIntervalUnit,
        onScheduledTimesChange = viewModel::updateScheduledTimes,
        onEndTimeChange = viewModel::updateEndTime,
        onActiveChange = viewModel::updateIsActive,
        onSave = {
            viewModel.saveHabit(
                onSuccess = { onSaveSuccess() },
                onError = { /* Error handled in UI state */ }
            )
        },
        onNavigateBack = onNavigateBack
    )
}

/**
 * Stateless content for HabitEditScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitEditContent(
    uiState: HabitEditUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onFrequencyTypeChange: (FrequencyType) -> Unit,
    onIntervalValueChange: (Int, TimeUnit) -> Unit,
    onIntervalUnitChange: (TimeUnit) -> Unit,
    onScheduledTimesChange: (List<LocalTime>) -> Unit,
    onEndTimeChange: (LocalTime?) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val descriptionFocusRequester = remember { FocusRequester() }
    
    Scaffold(
        topBar = {
            HabitEditTopBar(
                isEditMode = uiState.editHabitId != null,
                isSaving = uiState.isSaving,
                onNavigateBack = onNavigateBack,
                onSave = onSave
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> LoadingState()
                else -> HabitEditForm(
                    uiState = uiState,
                    descriptionFocusRequester = descriptionFocusRequester,
                    onNameChange = onNameChange,
                    onDescriptionChange = onDescriptionChange,
                    onColorChange = onColorChange,
                    onFrequencyTypeChange = onFrequencyTypeChange,
                    onIntervalValueChange = onIntervalValueChange,
                    onIntervalUnitChange = onIntervalUnitChange,
                    onScheduledTimesChange = onScheduledTimesChange,
                    onEndTimeChange = onEndTimeChange,
                    onActiveChange = onActiveChange,
                    onClearFocus = { focusManager.clearFocus() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitEditTopBar(
    isEditMode: Boolean,
    isSaving: Boolean,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit
) {
    TopAppBar(
        title = { 
            Text(
                if (isEditMode) 
                    stringResource(Res.string.edit_habit) 
                else 
                    stringResource(Res.string.add_habit)
            ) 
        },
        navigationIcon = {
            TextButton(onClick = onNavigateBack) {
                Text(stringResource(Res.string.cancel))
            }
        },
        actions = {
            TextButton(
                onClick = onSave,
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(Res.string.save))
                }
            }
        }
    )
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
private fun HabitEditForm(
    uiState: HabitEditUiState,
    descriptionFocusRequester: FocusRequester,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onFrequencyTypeChange: (FrequencyType) -> Unit,
    onIntervalValueChange: (Int, TimeUnit) -> Unit,
    onIntervalUnitChange: (TimeUnit) -> Unit,
    onScheduledTimesChange: (List<LocalTime>) -> Unit,
    onEndTimeChange: (LocalTime?) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onClearFocus: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClearFocus()
            },
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Name field
        NameField(
            name = uiState.name,
            nameError = uiState.nameError,
            onNameChange = onNameChange,
            onNext = { descriptionFocusRequester.requestFocus() }
        )

        // Description field
        DescriptionField(
            description = uiState.description,
            focusRequester = descriptionFocusRequester,
            onDescriptionChange = onDescriptionChange,
            onDone = onClearFocus
        )

        // Color selection
        ColorSelection(
            selectedColor = uiState.color,
            onColorChange = onColorChange
        )

        // Scheduling configuration
        SchedulingSection(
            frequencyType = uiState.frequencyType,
            intervalValue = uiState.intervalValue,
            intervalUnit = uiState.intervalUnit,
            scheduledTimes = uiState.scheduledTimes,
            endTime = uiState.endTime,
            onFrequencyTypeChange = onFrequencyTypeChange,
            onIntervalValueChange = onIntervalValueChange,
            onIntervalUnitChange = onIntervalUnitChange,
            onScheduledTimesChange = onScheduledTimesChange,
            onEndTimeChange = onEndTimeChange
        )

        // Active status
        ActiveStatusSwitch(
            isActive = uiState.isActive,
            onActiveChange = onActiveChange
        )

        // Error message
        uiState.saveError?.let { error ->
            ErrorCard(error = error)
        }
    }
}

@Composable
private fun NameField(
    name: String,
    nameError: String?,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(Res.string.habit_name_required)) },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            supportingText = nameError?.let { { Text(stringResource(Res.string.name_is_required)) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onNext() })
        )
    }
}

@Composable
private fun DescriptionField(
    description: String,
    focusRequester: FocusRequester,
    onDescriptionChange: (String) -> Unit,
    onDone: () -> Unit
) {
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text(stringResource(Res.string.description_optional)) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        minLines = 3,
        maxLines = 5,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() })
    )
}

@Composable
private fun ColorSelection(
    selectedColor: String,
    onColorChange: (String) -> Unit
) {
    val colorOptions = listOf(
        "#2196F3", // Blue
        "#4CAF50", // Green
        "#FF9800", // Orange
        "#9C27B0", // Purple
        "#F44336", // Red
        "#00BCD4", // Cyan
        "#FFEB3B", // Yellow
        "#795548", // Brown
        "#607D8B", // Blue Grey
        "#E91E63"  // Pink
    )
    
    Column {
        Text(
            text = stringResource(Res.string.choose_color),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(colorOptions) { colorHex ->
                ColorOption(
                    color = colorHex,
                    isSelected = selectedColor == colorHex,
                    onClick = { onColorChange(colorHex) }
                )
            }
        }
    }
}

@Composable
private fun ActiveStatusSwitch(
    isActive: Boolean,
    onActiveChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = stringResource(Res.string.active),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(Res.string.enable_tracking),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isActive,
            onCheckedChange = onActiveChange
        )
    }
}

@Composable
private fun ErrorCard(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = error,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// ========== Screen-Level Previews ==========

@Preview
@Composable
private fun HabitEditScreenAddModePreview() {
    MaterialTheme {
        HabitEditContent(
            uiState = HabitEditUiState(
                editHabitId = null,
                name = "",
                description = "",
                color = "#2196F3",
                isActive = true,
                frequencyType = FrequencyType.ONCE_DAILY,
                intervalMinutes = 1440,
                intervalUnit = TimeUnit.HOURS,
                scheduledTimes = listOf(LocalTime(9, 0)),
                endTime = null,
                nameError = null,
                saveError = null,
                isSaving = false,
                isLoading = false
            ),
            onNameChange = {},
            onDescriptionChange = {},
            onColorChange = {},
            onFrequencyTypeChange = {},
            onIntervalValueChange = { _, _ -> },
            onIntervalUnitChange = {},
            onScheduledTimesChange = {},
            onEndTimeChange = {},
            onActiveChange = {},
            onSave = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun HabitEditScreenEditModePreview() {
    MaterialTheme {
        HabitEditContent(
            uiState = HabitEditUiState(
                editHabitId = 1L,
                name = "Drink Water",
                description = "Stay hydrated throughout the day",
                color = "#2196F3",
                isActive = true,
                frequencyType = FrequencyType.HOURLY,
                intervalMinutes = 120,
                intervalUnit = TimeUnit.HOURS,
                scheduledTimes = listOf(LocalTime(8, 0)),
                endTime = LocalTime(22, 0),
                nameError = null,
                saveError = null,
                isSaving = false,
                isLoading = false
            ),
            onNameChange = {},
            onDescriptionChange = {},
            onColorChange = {},
            onFrequencyTypeChange = {},
            onIntervalValueChange = { _, _ -> },
            onIntervalUnitChange = {},
            onScheduledTimesChange = {},
            onEndTimeChange = {},
            onActiveChange = {},
            onSave = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun HabitEditScreenErrorStatePreview() {
    MaterialTheme {
        HabitEditContent(
            uiState = HabitEditUiState(
                editHabitId = null,
                name = "",
                description = "",
                color = "#2196F3",
                isActive = true,
                frequencyType = FrequencyType.ONCE_DAILY,
                intervalMinutes = 1440,
                intervalUnit = TimeUnit.HOURS,
                scheduledTimes = listOf(LocalTime(9, 0)),
                endTime = null,
                nameError = "Name is required",
                saveError = "Failed to save habit",
                isSaving = false,
                isLoading = false
            ),
            onNameChange = {},
            onDescriptionChange = {},
            onColorChange = {},
            onFrequencyTypeChange = {},
            onIntervalValueChange = { _, _ -> },
            onIntervalUnitChange = {},
            onScheduledTimesChange = {},
            onEndTimeChange = {},
            onActiveChange = {},
            onSave = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun HabitEditScreenLoadingStatePreview() {
    MaterialTheme {
        HabitEditContent(
            uiState = HabitEditUiState(
                editHabitId = 1L,
                name = "",
                description = "",
                color = "#2196F3",
                isActive = true,
                frequencyType = FrequencyType.ONCE_DAILY,
                intervalMinutes = 1440,
                intervalUnit = TimeUnit.HOURS,
                scheduledTimes = listOf(LocalTime(9, 0)),
                endTime = null,
                nameError = null,
                saveError = null,
                isSaving = false,
                isLoading = true
            ),
            onNameChange = {},
            onDescriptionChange = {},
            onColorChange = {},
            onFrequencyTypeChange = {},
            onIntervalValueChange = { _, _ -> },
            onIntervalUnitChange = {},
            onScheduledTimesChange = {},
            onEndTimeChange = {},
            onActiveChange = {},
            onSave = {},
            onNavigateBack = {}
        )
    }
}