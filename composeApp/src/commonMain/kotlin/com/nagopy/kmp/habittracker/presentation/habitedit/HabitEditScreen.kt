package com.nagopy.kmp.habittracker.presentation.habitedit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.presentation.habitedit.TimeUnit
import com.nagopy.kmp.habittracker.presentation.ui.parseColor
import kotlinx.datetime.LocalTime

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
                title = { Text(if (uiState.editHabitId != null) "Edit Habit" else "Add Habit") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel")
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
                            Text("Save")
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            // Name field
            Column {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Habit Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } }
                )
            }

            // Description field
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Color selection
            Column {
                Text(
                    text = "Choose Color",
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
                    text = "Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Frequency Type selection
                Column {
                    Text(
                        text = "Frequency",
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
                                        FrequencyType.ONCE_DAILY -> "Once Daily"
                                        FrequencyType.HOURLY -> "Hourly"
                                        FrequencyType.INTERVAL -> "Custom Interval"
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
                            text = if (uiState.frequencyType == FrequencyType.HOURLY) "Every" else "Interval:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        OutlinedTextField(
                            value = uiState.intervalValue.toString(),
                            onValueChange = { 
                                val value = it.toIntOrNull()
                                if (value != null && value > 0) {
                                    viewModel.updateIntervalValue(value, uiState.intervalUnit)
                                }
                            },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = uiState.frequencyType != FrequencyType.HOURLY
                        )
                        
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
                                        TimeUnit.MINUTES -> "minutes"
                                        TimeUnit.HOURS -> "hours"
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
                                        text = { Text("minutes") },
                                        onClick = {
                                            viewModel.updateIntervalUnit(TimeUnit.MINUTES)
                                            expanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("hours") },
                                        onClick = {
                                            viewModel.updateIntervalUnit(TimeUnit.HOURS)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "hour",
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
                        text = if (uiState.frequencyType == FrequencyType.ONCE_DAILY) "Scheduled Times" else "Start Time",
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
                                    Text("Remove")
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
                            Text("Add Time")
                        }
                        
                        if (showTimePicker) {
                            val timePickerState = rememberTimePickerState(
                                initialHour = 9,
                                initialMinute = 0,
                                is24Hour = true
                            )
                            
                            AlertDialog(
                                onDismissRequest = { showTimePicker = false },
                                title = { Text("Add Time") },
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
                                        Text("Add")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showTimePicker = false }) {
                                        Text("Cancel")
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
                            Text("Change Start Time")
                        }
                        
                        if (showTimePicker) {
                            val timePickerState = rememberTimePickerState(
                                initialHour = uiState.scheduledTimes.first().hour,
                                initialMinute = uiState.scheduledTimes.first().minute,
                                is24Hour = true
                            )
                            
                            AlertDialog(
                                onDismissRequest = { showTimePicker = false },
                                title = { Text("Set Start Time") },
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
                                        Text("Set")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showTimePicker = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
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
                        text = "Active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Enable tracking for this habit",
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
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}