package com.nagopy.kmp.habittracker.presentation.habitedit.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.presentation.habitedit.TimeUnit
import habittracker.composeapp.generated.resources.Res
import habittracker.composeapp.generated.resources.*
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulingSection(
    frequencyType: FrequencyType,
    intervalValue: Int,
    intervalUnit: TimeUnit,
    scheduledTimes: List<LocalTime>,
    endTime: LocalTime?,
    onFrequencyTypeChange: (FrequencyType) -> Unit,
    onIntervalValueChange: (Int, TimeUnit) -> Unit,
    onIntervalUnitChange: (TimeUnit) -> Unit,
    onScheduledTimesChange: (List<LocalTime>) -> Unit,
    onEndTimeChange: (LocalTime?) -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.schedule),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Frequency Type selection
        FrequencyTypeSelection(
            frequencyType = frequencyType,
            onFrequencyTypeChange = onFrequencyTypeChange
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Interval Configuration
        if (frequencyType == FrequencyType.HOURLY || frequencyType == FrequencyType.INTERVAL) {
            IntervalConfiguration(
                frequencyType = frequencyType,
                intervalValue = intervalValue,
                intervalUnit = intervalUnit,
                onIntervalValueChange = onIntervalValueChange,
                onIntervalUnitChange = onIntervalUnitChange
            )
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Scheduled Times
        ScheduledTimesConfiguration(
            frequencyType = frequencyType,
            scheduledTimes = scheduledTimes,
            endTime = endTime,
            onScheduledTimesChange = onScheduledTimesChange,
            onEndTimeChange = onEndTimeChange
        )
    }
}

@Composable
private fun FrequencyTypeSelection(
    frequencyType: FrequencyType,
    onFrequencyTypeChange: (FrequencyType) -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.frequency),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FrequencyType.entries.forEach { frequency ->
                FilterChip(
                    onClick = { onFrequencyTypeChange(frequency) },
                    label = { 
                        Text(when(frequency) {
                            FrequencyType.ONCE_DAILY -> stringResource(Res.string.once_daily)
                            FrequencyType.HOURLY -> stringResource(Res.string.hourly)
                            FrequencyType.INTERVAL -> stringResource(Res.string.custom_interval)
                        })
                    },
                    selected = frequencyType == frequency
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntervalConfiguration(
    frequencyType: FrequencyType,
    intervalValue: Int,
    intervalUnit: TimeUnit,
    onIntervalValueChange: (Int, TimeUnit) -> Unit,
    onIntervalUnitChange: (TimeUnit) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (frequencyType == FrequencyType.HOURLY) 
                stringResource(Res.string.every) 
            else 
                stringResource(Res.string.interval),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Interval value selector button
        var showIntervalPicker by remember { mutableStateOf(false) }
        
        OutlinedButton(
            onClick = { showIntervalPicker = true },
            modifier = Modifier.width(80.dp),
            enabled = frequencyType != FrequencyType.HOURLY
        ) {
            Text(intervalValue.toString())
        }
        
        // Interval picker dialog
        if (showIntervalPicker) {
            IntervalPickerDialog(
                currentValue = intervalValue,
                unit = intervalUnit,
                frequencyType = frequencyType,
                onValueAndUnitChange = onIntervalValueChange,
                onDismiss = { showIntervalPicker = false }
            )
        }
        
        // Unit selector dropdown
        if (frequencyType != FrequencyType.HOURLY) {
            var expanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.width(100.dp)
            ) {
                OutlinedTextField(
                    value = when (intervalUnit) {
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
                            onIntervalUnitChange(TimeUnit.MINUTES)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.hours)) },
                        onClick = {
                            onIntervalUnitChange(TimeUnit.HOURS)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduledTimesConfiguration(
    frequencyType: FrequencyType,
    scheduledTimes: List<LocalTime>,
    endTime: LocalTime?,
    onScheduledTimesChange: (List<LocalTime>) -> Unit,
    onEndTimeChange: (LocalTime?) -> Unit
) {
    Column {
        Text(
            text = if (frequencyType == FrequencyType.ONCE_DAILY) 
                stringResource(Res.string.scheduled_times) 
            else 
                stringResource(Res.string.start_time),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        // Display current scheduled times
        scheduledTimes.forEach { time ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (scheduledTimes.size > 1) {
                    TextButton(
                        onClick = {
                            onScheduledTimesChange(scheduledTimes.filter { it != time })
                        }
                    ) {
                        Text(stringResource(Res.string.remove))
                    }
                }
            }
        }
        
        // Time picker buttons
        if (frequencyType == FrequencyType.ONCE_DAILY) {
            // Add time button for ONCE_DAILY
            AddTimeButton(
                scheduledTimes = scheduledTimes,
                onScheduledTimesChange = onScheduledTimesChange
            )
        } else {
            // Change start time button for other types
            ChangeStartTimeButton(
                currentTime = scheduledTimes.firstOrNull() ?: LocalTime(9, 0),
                onTimeChange = { newTime ->
                    onScheduledTimesChange(listOf(newTime))
                }
            )
            
            // End time configuration for interval-based habits
            if (frequencyType == FrequencyType.HOURLY || frequencyType == FrequencyType.INTERVAL) {
                EndTimeConfiguration(
                    endTime = endTime,
                    onEndTimeChange = onEndTimeChange
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTimeButton(
    scheduledTimes: List<LocalTime>,
    onScheduledTimesChange: (List<LocalTime>) -> Unit
) {
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
                        if (!scheduledTimes.contains(newTime)) {
                            onScheduledTimesChange(scheduledTimes + newTime)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeStartTimeButton(
    currentTime: LocalTime,
    onTimeChange: (LocalTime) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    OutlinedButton(
        onClick = { showTimePicker = true }
    ) {
        Text(stringResource(Res.string.change_start_time))
    }
    
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.hour,
            initialMinute = currentTime.minute,
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
                        onTimeChange(LocalTime(timePickerState.hour, timePickerState.minute))
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EndTimeConfiguration(
    endTime: LocalTime?,
    onEndTimeChange: (LocalTime?) -> Unit
) {
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { showEndTimePicker = true }
        ) {
            Text(
                if (endTime != null) 
                    stringResource(Res.string.change_end_time) 
                else 
                    stringResource(Res.string.set_end_time)
            )
        }
        
        endTime?.let { time ->
            Text(
                text = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = { onEndTimeChange(null) }
            ) {
                Text(stringResource(Res.string.clear))
            }
        }
    }
    
    if (showEndTimePicker) {
        val endTimePickerState = rememberTimePickerState(
            initialHour = endTime?.hour ?: 17,
            initialMinute = endTime?.minute ?: 0,
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
                        onEndTimeChange(LocalTime(endTimePickerState.hour, endTimePickerState.minute))
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