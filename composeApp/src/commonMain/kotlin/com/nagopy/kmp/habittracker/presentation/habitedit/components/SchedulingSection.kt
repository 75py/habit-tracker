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
import org.jetbrains.compose.ui.tooling.preview.Preview

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
        if (frequencyType == FrequencyType.INTERVAL) {
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
    // For HOURLY, don't show the interval configuration as per requirement
    if (frequencyType == FrequencyType.HOURLY) {
        return
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(Res.string.interval),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Interval value selector button
        var showIntervalPicker by remember { mutableStateOf(false) }
        
        OutlinedButton(
            onClick = { showIntervalPicker = true },
            modifier = Modifier.width(80.dp)
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
        
        if (frequencyType == FrequencyType.ONCE_DAILY) {
            // ONCE_DAILY: Show each time with change and delete buttons
            scheduledTimes.forEach { time ->
                ScheduledTimeItem(
                    time = time,
                    onTimeChange = { newTime ->
                        val updatedTimes = scheduledTimes.map { if (it == time) newTime else it }
                        onScheduledTimesChange(updatedTimes)
                    },
                    onTimeDelete = {
                        onScheduledTimesChange(scheduledTimes.filter { it != time })
                    }
                )
            }
            
            // Add time button for ONCE_DAILY
            AddTimeButton(
                scheduledTimes = scheduledTimes,
                onScheduledTimesChange = onScheduledTimesChange
            )
        } else {
            // HOURLY and INTERVAL: Use start time configuration similar to end time
            StartTimeConfiguration(
                startTime = scheduledTimes.firstOrNull(),
                onStartTimeChange = { newTime ->
                    onScheduledTimesChange(if (newTime != null) listOf(newTime) else emptyList())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduledTimeItem(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    onTimeDelete: () -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { showTimePicker = true }
        ) {
            Text(stringResource(Res.string.change_time))
        }
        
        Text(
            text = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.bodyMedium
        )
        
        TextButton(
            onClick = onTimeDelete
        ) {
            Text(stringResource(Res.string.remove))
        }
    }
    
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = true
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(Res.string.set_time)) },
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
private fun StartTimeConfiguration(
    startTime: LocalTime?,
    onStartTimeChange: (LocalTime?) -> Unit
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { showStartTimePicker = true }
        ) {
            Text(
                if (startTime != null) 
                    stringResource(Res.string.change_start_time) 
                else 
                    stringResource(Res.string.set_start_time)
            )
        }
        
        startTime?.let { time ->
            Text(
                text = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = { onStartTimeChange(null) }
            ) {
                Text(stringResource(Res.string.clear))
            }
        }
    }
    
    if (showStartTimePicker) {
        val startTimePickerState = rememberTimePickerState(
            initialHour = startTime?.hour ?: 9,
            initialMinute = startTime?.minute ?: 0,
            is24Hour = true
        )
        
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text(stringResource(Res.string.set_start_time)) },
            text = {
                TimePicker(state = startTimePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onStartTimeChange(LocalTime(startTimePickerState.hour, startTimePickerState.minute))
                        showStartTimePicker = false
                    }
                ) {
                    Text(stringResource(Res.string.set))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}

// ========== Previews ==========

@Preview
@Composable
private fun SchedulingSectionOnceDailyPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            SchedulingSection(
                frequencyType = FrequencyType.ONCE_DAILY,
                intervalValue = 24,
                intervalUnit = TimeUnit.HOURS,
                scheduledTimes = listOf(LocalTime(9, 0), LocalTime(18, 0)),
                endTime = null,
                onFrequencyTypeChange = {},
                onIntervalValueChange = { _, _ -> },
                onIntervalUnitChange = {},
                onScheduledTimesChange = {},
                onEndTimeChange = {}
            )
        }
    }
}

@Preview
@Composable
private fun SchedulingSectionHourlyPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            SchedulingSection(
                frequencyType = FrequencyType.HOURLY,
                intervalValue = 2,
                intervalUnit = TimeUnit.HOURS,
                scheduledTimes = listOf(LocalTime(8, 0)),
                endTime = LocalTime(22, 0),
                onFrequencyTypeChange = {},
                onIntervalValueChange = { _, _ -> },
                onIntervalUnitChange = {},
                onScheduledTimesChange = {},
                onEndTimeChange = {}
            )
        }
    }
}

@Preview
@Composable
private fun SchedulingSectionIntervalPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            SchedulingSection(
                frequencyType = FrequencyType.INTERVAL,
                intervalValue = 30,
                intervalUnit = TimeUnit.MINUTES,
                scheduledTimes = listOf(LocalTime(9, 0)),
                endTime = LocalTime(17, 0),
                onFrequencyTypeChange = {},
                onIntervalValueChange = { _, _ -> },
                onIntervalUnitChange = {},
                onScheduledTimesChange = {},
                onEndTimeChange = {}
            )
        }
    }
}

@Preview
@Composable
private fun FrequencyTypeSelectionPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            FrequencyTypeSelection(
                frequencyType = FrequencyType.HOURLY,
                onFrequencyTypeChange = {}
            )
        }
    }
}

@Preview
@Composable
private fun IntervalConfigurationPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            IntervalConfiguration(
                frequencyType = FrequencyType.INTERVAL,
                intervalValue = 30,
                intervalUnit = TimeUnit.MINUTES,
                onIntervalValueChange = { _, _ -> },
                onIntervalUnitChange = {}
            )
        }
    }
}

@Preview
@Composable
private fun ScheduledTimesConfigurationOnceDailyPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ScheduledTimesConfiguration(
                frequencyType = FrequencyType.ONCE_DAILY,
                scheduledTimes = listOf(LocalTime(9, 0), LocalTime(18, 0)),
                endTime = null,
                onScheduledTimesChange = {},
                onEndTimeChange = {}
            )
        }
    }
}

@Preview
@Composable
private fun ScheduledTimesConfigurationHourlyPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ScheduledTimesConfiguration(
                frequencyType = FrequencyType.HOURLY,
                scheduledTimes = listOf(LocalTime(8, 0)),
                endTime = LocalTime(22, 0),
                onScheduledTimesChange = {},
                onEndTimeChange = {}
            )
        }
    }
}

@Preview
@Composable
private fun EndTimeConfigurationPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            EndTimeConfiguration(
                endTime = LocalTime(22, 0),
                onEndTimeChange = {}
            )
        }
    }
}

@Preview
@Composable
private fun EndTimeConfigurationNoTimePreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            EndTimeConfiguration(
                endTime = null,
                onEndTimeChange = {}
            )
        }
    }
}