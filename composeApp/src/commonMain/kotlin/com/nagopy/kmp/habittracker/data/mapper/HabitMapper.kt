package com.nagopy.kmp.habittracker.data.mapper

import com.nagopy.kmp.habittracker.data.local.HabitEntity
import com.nagopy.kmp.habittracker.data.local.LogEntity
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitBase
import com.nagopy.kmp.habittracker.domain.model.DailyHabit
import com.nagopy.kmp.habittracker.domain.model.HourlyHabit
import com.nagopy.kmp.habittracker.domain.model.IntervalHabit
import com.nagopy.kmp.habittracker.domain.model.HabitLog
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.model.HabitIntervalValidator
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Mappers to convert between domain models and data entities.
 */

// Habit mappers
fun HabitEntity.toDomainModel(): Habit {
    // Auto-detect frequency type based on intervalMinutes
    val detectedFrequencyType = when {
        intervalMinutes == 1440 -> FrequencyType.ONCE_DAILY // 24 hours = once daily
        intervalMinutes % 60 == 0 -> FrequencyType.HOURLY // Multiples of 60 minutes = hourly
        else -> FrequencyType.INTERVAL // Custom intervals
    }
    
    val habitBase = HabitBase(
        id = id,
        name = name,
        description = description,
        color = color,
        isActive = isActive,
        createdAt = LocalDate.parse(createdAt)
    )
    
    return when (detectedFrequencyType) {
        FrequencyType.ONCE_DAILY -> DailyHabit(
            base = habitBase,
            scheduledTimes = parseScheduledTimes(scheduledTimes)
        )
        FrequencyType.HOURLY -> HourlyHabit(
            base = habitBase,
            intervalMinutes = intervalMinutes,
            startTime = startTime?.let { parseTime(it) } ?: LocalTime(9, 0),
            endTime = endTime?.let { parseTime(it) }
        )
        FrequencyType.INTERVAL -> IntervalHabit(
            base = habitBase,
            intervalMinutes = intervalMinutes,
            startTime = startTime?.let { parseTime(it) } ?: LocalTime(9, 0),
            endTime = endTime?.let { parseTime(it) }
        )
    }
}

fun Habit.toEntity(): HabitEntity {
    return HabitEntity(
        id = id,
        name = name,
        description = description,
        color = color,
        isActive = isActive,
        createdAt = createdAt.toString(),
        intervalMinutes = when (this) {
            is DailyHabit -> HabitIntervalValidator.VALID_ONCE_DAILY_MINUTES
            is HourlyHabit -> intervalMinutes
            is IntervalHabit -> intervalMinutes
        },
        scheduledTimes = when (this) {
            is DailyHabit -> formatScheduledTimes(scheduledTimes)
            is HourlyHabit, is IntervalHabit -> "" // No scheduled times for interval-based habits
        },
        startTime = when (this) {
            is DailyHabit -> null // No start time for daily habits
            is HourlyHabit -> formatTime(startTime)
            is IntervalHabit -> formatTime(startTime)
        },
        endTime = when (this) {
            is DailyHabit -> null // No end time for daily habits
            is HourlyHabit -> endTime?.let { formatTime(it) }
            is IntervalHabit -> endTime?.let { formatTime(it) }
        }
    )
}

// Helper functions for time formatting
private fun parseScheduledTimes(timesString: String): List<LocalTime> {
    return if (timesString.isEmpty()) {
        listOf(LocalTime(9, 0)) // Default 9:00 AM
    } else {
        timesString.split(",").mapNotNull { timeStr ->
            try {
                val parts = timeStr.trim().split(":")
                if (parts.size == 2) {
                    LocalTime(parts[0].toInt(), parts[1].toInt())
                } else null
            } catch (e: Exception) {
                null
            }
        }.ifEmpty { listOf(LocalTime(9, 0)) }
    }
}

private fun formatScheduledTimes(times: List<LocalTime>): String {
    return times.joinToString(",") { time ->
        "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
    }
}

private fun parseTime(timeString: String): LocalTime? {
    return try {
        val parts = timeString.trim().split(":")
        if (parts.size == 2) {
            LocalTime(parts[0].toInt(), parts[1].toInt())
        } else null
    } catch (e: Exception) {
        null
    }
}

private fun formatTime(time: LocalTime): String {
    return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

// HabitLog mappers
fun LogEntity.toDomainModel(): HabitLog {
    return HabitLog(
        id = id,
        habitId = habitId,
        date = LocalDate.parse(date),
        isCompleted = isCompleted
    )
}

fun HabitLog.toEntity(): LogEntity {
    return LogEntity(
        id = id,
        habitId = habitId,
        date = date.toString(),
        isCompleted = isCompleted
    )
}