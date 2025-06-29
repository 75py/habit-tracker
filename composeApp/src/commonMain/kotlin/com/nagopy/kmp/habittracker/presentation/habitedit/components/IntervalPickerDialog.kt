package com.nagopy.kmp.habittracker.presentation.habitedit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.model.HabitIntervalValidator
import com.nagopy.kmp.habittracker.presentation.habitedit.TimeUnit
import habittracker.composeapp.generated.resources.Res
import habittracker.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Helper function to convert and clamp value when switching between time units
 */
private fun convertAndClampValue(
    currentValue: Int,
    fromUnit: TimeUnit,
    toUnit: TimeUnit,
    frequencyType: FrequencyType
): Int {
    return when {
        fromUnit == toUnit -> currentValue
        fromUnit == TimeUnit.HOURS && toUnit == TimeUnit.MINUTES -> {
            // Convert hours to minutes and clamp to valid range
            val minutesValidValues = if (frequencyType == FrequencyType.INTERVAL) {
                HabitIntervalValidator.getAllValidIntervalMinutes().filter { it <= 60 }
            } else {
                (1..60).toList()
            }
            minutesValidValues.minByOrNull { kotlin.math.abs(it - (currentValue * 60)) } ?: 1
        }
        fromUnit == TimeUnit.MINUTES && toUnit == TimeUnit.HOURS -> {
            // Convert minutes to hours and clamp to valid range
            val hoursValidValues = if (frequencyType == FrequencyType.INTERVAL) {
                listOf(1, 2, 3, 4, 6, 8, 12)
            } else {
                (1..24).toList()
            }
            val targetHours = currentValue / 60
            hoursValidValues.minByOrNull { kotlin.math.abs(it - targetHours) } ?: 1
        }
        else -> currentValue
    }
}

@Composable
fun IntervalPickerDialog(
    currentValue: Int,
    unit: TimeUnit,
    frequencyType: FrequencyType,
    onValueAndUnitChange: (Int, TimeUnit) -> Unit,
    onDismiss: () -> Unit
) {
    var tempUnit by remember { mutableStateOf(unit) }
    
    // Calculate reasonable min/max values based on current unit and frequency type
    val (minValue, maxValue) = when (tempUnit) {
        TimeUnit.MINUTES -> {
            if (frequencyType == FrequencyType.INTERVAL) {
                1 to 60 // For INTERVAL, only allow 1-60 minutes (the valid divisors)
            } else {
                1 to 1440 // 24 hours in minutes
            }
        }
        TimeUnit.HOURS -> {
            if (frequencyType == FrequencyType.INTERVAL) {
                1 to 12 // For INTERVAL, allow 1-12 hours (valid hour intervals)
            } else {
                1 to 24 // 24 hours
            }
        }
    }
    
    // Convert initial value if needed based on current unit
    val initialValue = when {
        unit == tempUnit -> currentValue
        unit == TimeUnit.MINUTES && tempUnit == TimeUnit.HOURS -> currentValue / 60
        unit == TimeUnit.HOURS && tempUnit == TimeUnit.MINUTES -> currentValue * 60
        else -> currentValue
    }
    
    var tempValue by remember { mutableIntStateOf(initialValue.coerceIn(minValue, maxValue)) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.select_interval)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Unit selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { 
                            if (tempUnit != TimeUnit.MINUTES) {
                                tempValue = convertAndClampValue(tempValue, tempUnit, TimeUnit.MINUTES, frequencyType)
                                tempUnit = TimeUnit.MINUTES
                            }
                        },
                        label = { Text(stringResource(Res.string.minutes)) },
                        selected = tempUnit == TimeUnit.MINUTES
                    )
                    FilterChip(
                        onClick = { 
                            if (tempUnit != TimeUnit.HOURS) {
                                tempValue = convertAndClampValue(tempValue, tempUnit, TimeUnit.HOURS, frequencyType)
                                tempUnit = TimeUnit.HOURS
                            }
                        },
                        label = { Text(stringResource(Res.string.hours)) },
                        selected = tempUnit == TimeUnit.HOURS
                    )
                }
                
                // Current value display
                Text(
                    text = "$tempValue ${when (tempUnit) {
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
                    // Define valid values for +/- buttons
                    val validValues = remember(tempUnit, frequencyType, maxValue) {
                        when (tempUnit) {
                            TimeUnit.MINUTES -> {
                                if (frequencyType == FrequencyType.INTERVAL) {
                                    HabitIntervalValidator.getAllValidIntervalMinutes().filter { it <= 60 }
                                } else {
                                    (1..minOf(maxValue, 60)).toList()
                                }
                            }
                            TimeUnit.HOURS -> {
                                if (frequencyType == FrequencyType.INTERVAL) {
                                    listOf(1, 2, 3, 4, 6, 8, 12)
                                } else {
                                    (1..maxValue).toList()
                                }
                            }
                        }
                    }
                    
                    // Helper to get current value index
                    val currentValueIndex = validValues.indexOf(tempValue)
                    
                    // Decrease button
                    FilledIconButton(
                        onClick = { 
                            if (currentValueIndex > 0) {
                                tempValue = validValues[currentValueIndex - 1]
                            }
                        },
                        enabled = currentValueIndex > 0
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
                            if (currentValueIndex >= 0 && currentValueIndex < validValues.size - 1) {
                                tempValue = validValues[currentValueIndex + 1]
                            }
                        },
                        enabled = currentValueIndex >= 0 && currentValueIndex < validValues.size - 1
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
                    val quickValues = when (tempUnit) {
                        TimeUnit.MINUTES -> {
                            if (frequencyType == FrequencyType.INTERVAL) {
                                // For INTERVAL type, only show valid divisors of 60 in minutes
                                HabitIntervalValidator.getAllValidIntervalMinutes()
                            } else {
                                listOf(1, 5, 10, 15, 30, 60)
                            }
                        }
                        TimeUnit.HOURS -> {
                            if (frequencyType == FrequencyType.INTERVAL) {
                                // For INTERVAL type, show common hour intervals
                                listOf(1, 2, 3, 4, 6, 8, 12)
                            } else {
                                listOf(1, 2, 3, 4, 6, 8, 12, 24)
                            }
                        }
                    }
                    
                    items(quickValues) { value ->
                        val currentMaxValue = when (tempUnit) {
                            TimeUnit.MINUTES -> if (frequencyType == FrequencyType.INTERVAL) 60 else 1440
                            TimeUnit.HOURS -> if (frequencyType == FrequencyType.INTERVAL) 12 else 24
                        }
                        if (value <= currentMaxValue) {
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
                    onValueAndUnitChange(tempValue, tempUnit)
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

// ========== Previews ==========

@Preview
@Composable
private fun IntervalPickerDialogHourlyPreview() {
    MaterialTheme {
        IntervalPickerDialog(
            currentValue = 2,
            unit = TimeUnit.HOURS,
            frequencyType = FrequencyType.INTERVAL,
            onValueAndUnitChange = { _, _ -> },
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun IntervalPickerDialogIntervalMinutesPreview() {
    MaterialTheme {
        IntervalPickerDialog(
            currentValue = 30,
            unit = TimeUnit.MINUTES,
            frequencyType = FrequencyType.INTERVAL,
            onValueAndUnitChange = { _, _ -> },
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun IntervalPickerDialogIntervalHoursPreview() {
    MaterialTheme {
        IntervalPickerDialog(
            currentValue = 1,
            unit = TimeUnit.HOURS,
            frequencyType = FrequencyType.INTERVAL,
            onValueAndUnitChange = { _, _ -> },
            onDismiss = {}
        )
    }
}