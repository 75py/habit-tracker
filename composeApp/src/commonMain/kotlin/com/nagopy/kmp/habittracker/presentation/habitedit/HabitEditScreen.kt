package com.nagopy.kmp.habittracker.presentation.habitedit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.presentation.habitedit.TimeUnit
import com.nagopy.kmp.habittracker.presentation.ui.parseColor
import habittracker.composeapp.generated.resources.Res
import habittracker.composeapp.generated.resources.*
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

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
    
    // Focus management
    val focusManager = LocalFocusManager.current
    val descriptionFocusRequester = remember { FocusRequester() }
    
    // Load habit for editing if habitId is provided
    LaunchedEffect(habitId) {
        if (habitId != null) {
            viewModel.loadHabitForEdit(habitId)
        }
    }
    
    // Predefined color options
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.editHabitId != null) stringResource(Res.string.edit_habit) else stringResource(Res.string.add_habit)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(stringResource(Res.string.cancel))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveHabit(
                                onSuccess = { onSaveSuccess() },
                                onError = { /* Error handled in UI state */ }
                            )
                        },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
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
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus()
                    },
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            // Name field
            Column {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text(stringResource(Res.string.habit_name_required)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(stringResource(Res.string.name_is_required)) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            descriptionFocusRequester.requestFocus()
                        }
                    )
                )
            }

            // Description field
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text(stringResource(Res.string.description_optional)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(descriptionFocusRequester),
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                )
            )

            // Color selection
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
                            isSelected = uiState.color == colorHex,
                            onClick = { viewModel.updateColor(colorHex) }
                        )
                    }
                }
            }

            // Scheduling configuration
            Column {
                Text(
                    text = stringResource(Res.string.schedule),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Frequency Type selection
                Column {
                    Text(
                        text = stringResource(Res.string.frequency),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Frequency type chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FrequencyType.entries.forEach { frequency ->
                            FilterChip(
                                onClick = { viewModel.updateFrequencyType(frequency) },
                                label = { 
                                    Text(when(frequency) {
                                        FrequencyType.ONCE_DAILY -> stringResource(Res.string.once_daily)
                                        FrequencyType.HOURLY -> stringResource(Res.string.hourly)
                                        FrequencyType.INTERVAL -> stringResource(Res.string.custom_interval)
                                    })
                                },
                                selected = uiState.frequencyType == frequency
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Interval Configuration (for HOURLY and INTERVAL types)
                if (uiState.frequencyType == FrequencyType.HOURLY || uiState.frequencyType == FrequencyType.INTERVAL) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (uiState.frequencyType == FrequencyType.HOURLY) stringResource(Res.string.every) else stringResource(Res.string.interval),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Interval value selector button
                        var showIntervalPicker by remember { mutableStateOf(false) }
                        
                        OutlinedButton(
                            onClick = { showIntervalPicker = true },
                            modifier = Modifier.width(80.dp),
                            enabled = uiState.frequencyType != FrequencyType.HOURLY
                        ) {
                            Text(uiState.intervalValue.toString())
                        }
                        
                        // Interval picker dialog
                        if (showIntervalPicker) {
                            IntervalPickerDialog(
                                currentValue = uiState.intervalValue,
                                unit = uiState.intervalUnit,
                                onValueChange = { value ->
                                    viewModel.updateIntervalValue(value, uiState.intervalUnit)
                                },
                                onDismiss = { showIntervalPicker = false }
                            )
                        }
                        
                        // Unit selector dropdown
                        if (uiState.frequencyType != FrequencyType.HOURLY) {
                            var expanded by remember { mutableStateOf(false) }
                            
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.width(100.dp)
                            ) {
                                OutlinedTextField(
                                    value = when (uiState.intervalUnit) {
                                        TimeUnit.MINUTES -> stringResource(Res.string.minutes)
                                        TimeUnit.HOURS -> stringResource(Res.string.hours)
                                    },
                                    onValueChange = { },
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.minutes)) },
                                        onClick = {
                                            viewModel.updateIntervalUnit(TimeUnit.MINUTES)
                                            expanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.hours)) },
                                        onClick = {
                                            viewModel.updateIntervalUnit(TimeUnit.HOURS)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(Res.string.hour),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Scheduled Times
                Column {
                    Text(
                        text = if (uiState.frequencyType == FrequencyType.ONCE_DAILY) stringResource(Res.string.scheduled_times) else stringResource(Res.string.start_time),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Display current scheduled times
                    uiState.scheduledTimes.forEach { time ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            if (uiState.scheduledTimes.size > 1) {
                                TextButton(
                                    onClick = {
                                        viewModel.updateScheduledTimes(
                                            uiState.scheduledTimes.filter { it != time }
                                        )
                                    }
                                ) {
                                    Text(stringResource(Res.string.remove))
                                }
                            }
                        }
                    }
                    
                    // Add time button (only for ONCE_DAILY type)
                    if (uiState.frequencyType == FrequencyType.ONCE_DAILY) {
                        var showTimePicker by remember { mutableStateOf(false) }
                        
                        OutlinedButton(
                            onClick = { showTimePicker = true }
                        ) {
                            Text(stringResource(Res.string.add_time))
                        }
                        
                        if (showTimePicker) {
                            val timePickerState = rememberTimePickerState(
                                initialHour = 9,
                                initialMinute = 0,
                                is24Hour = true
                            )
                            
                            AlertDialog(
                                onDismissRequest = { showTimePicker = false },
                                title = { Text(stringResource(Res.string.add_time)) },
                                text = {
                                    TimePicker(state = timePickerState)
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            val newTime = LocalTime(timePickerState.hour, timePickerState.minute)
                                            if (!uiState.scheduledTimes.contains(newTime)) {
                                                viewModel.updateScheduledTimes(
                                                    uiState.scheduledTimes + newTime
                                                )
                                            }
                                            showTimePicker = false
                                        }
                                    ) {
                                        Text(stringResource(Res.string.add))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showTimePicker = false }) {
                                        Text(stringResource(Res.string.cancel))
                                    }
                                }
                            )
                        }
                    } else {
                        // For non-daily types, allow editing the single start time
                        var showTimePicker by remember { mutableStateOf(false) }
                        
                        OutlinedButton(
                            onClick = { showTimePicker = true }
                        ) {
                            Text(stringResource(Res.string.change_start_time))
                        }
                        
                        if (showTimePicker) {
                            val timePickerState = rememberTimePickerState(
                                initialHour = uiState.scheduledTimes.first().hour,
                                initialMinute = uiState.scheduledTimes.first().minute,
                                is24Hour = true
                            )
                            
                            AlertDialog(
                                onDismissRequest = { showTimePicker = false },
                                title = { Text(stringResource(Res.string.set_start_time)) },
                                text = {
                                    TimePicker(state = timePickerState)
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.updateScheduledTimes(
                                                listOf(LocalTime(timePickerState.hour, timePickerState.minute))
                                            )
                                            showTimePicker = false
                                        }
                                    ) {
                                        Text(stringResource(Res.string.set))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showTimePicker = false }) {
                                        Text(stringResource(Res.string.cancel))
                                    }
                                }
                            )
                        }
                        
                        // End time picker for interval-based habits
                        if (uiState.frequencyType == FrequencyType.HOURLY || uiState.frequencyType == FrequencyType.INTERVAL) {
                            var showEndTimePicker by remember { mutableStateOf(false) }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showEndTimePicker = true }
                                ) {
                                    Text(if (uiState.endTime != null) stringResource(Res.string.change_end_time) else stringResource(Res.string.set_end_time))
                                }
                                
                                uiState.endTime?.let { endTime ->
                                    Text(
                                        text = "${endTime.hour.toString().padStart(2, '0')}:${endTime.minute.toString().padStart(2, '0')}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    TextButton(
                                        onClick = { viewModel.updateEndTime(null) }
                                    ) {
                                        Text(stringResource(Res.string.clear))
                                    }
                                }
                            }
                            
                            if (showEndTimePicker) {
                                val endTimePickerState = rememberTimePickerState(
                                    initialHour = uiState.endTime?.hour ?: 17,
                                    initialMinute = uiState.endTime?.minute ?: 0,
                                    is24Hour = true
                                )
                                
                                AlertDialog(
                                    onDismissRequest = { showEndTimePicker = false },
                                    title = { Text(stringResource(Res.string.set_end_time)) },
                                    text = {
                                        TimePicker(state = endTimePickerState)
                                    },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                viewModel.updateEndTime(
                                                    LocalTime(endTimePickerState.hour, endTimePickerState.minute)
                                                )
                                                showEndTimePicker = false
                                            }
                                        ) {
                                            Text(stringResource(Res.string.set))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showEndTimePicker = false }) {
                                            Text(stringResource(Res.string.cancel))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Active status
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
                    checked = uiState.isActive,
                    onCheckedChange = { viewModel.updateIsActive(it) }
                )
            }

            // Error message
            uiState.saveError?.let { error ->
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
            }
        }
    }
}

@Composable
private fun ColorOption(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val parsedColor = parseColor(color)
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(parsedColor)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(Res.string.selected),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun IntervalPickerDialog(
    currentValue: Int,
    unit: TimeUnit,
    onValueChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Calculate reasonable min/max values based on unit
    val minValue = 1
    val maxValue = when (unit) {
        TimeUnit.MINUTES -> 1440 // 24 hours in minutes
        TimeUnit.HOURS -> 24 // 24 hours
    }
    
    var tempValue by remember { mutableIntStateOf(currentValue.coerceIn(minValue, maxValue)) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.select_interval)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current value display
                Text(
                    text = "$tempValue ${when (unit) {
                        TimeUnit.MINUTES -> stringResource(Res.string.minutes)
                        TimeUnit.HOURS -> stringResource(Res.string.hours)
                    }}",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                
                // Increment/Decrement controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decrease button
                    FilledIconButton(
                        onClick = { 
                            if (tempValue > minValue) {
                                tempValue--
                            }
                        },
                        enabled = tempValue > minValue
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = stringResource(Res.string.decrease)
                        )
                    }
                    
                    // Current value
                    Text(
                        text = tempValue.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(80.dp)
                    )
                    
                    // Increase button
                    FilledIconButton(
                        onClick = { 
                            if (tempValue < maxValue) {
                                tempValue++
                            }
                        },
                        enabled = tempValue < maxValue
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(Res.string.increase)
                        )
                    }
                }
                
                // Quick value buttons for common intervals
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickValues = when (unit) {
                        TimeUnit.MINUTES -> listOf(1, 5, 10, 15, 30, 60)
                        TimeUnit.HOURS -> listOf(1, 2, 3, 4, 6, 8, 12, 24)
                    }
                    
                    items(quickValues) { value ->
                        if (value <= maxValue) {
                            FilterChip(
                                onClick = { tempValue = value },
                                label = { Text(value.toString()) },
                                selected = tempValue == value
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onValueChange(tempValue)
                    onDismiss()
                }
            ) {
                Text(stringResource(Res.string.set))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}