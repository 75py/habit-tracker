package com.nagopy.kmp.habittracker.data.mapper

import com.nagopy.kmp.habittracker.data.local.HabitEntity
import com.nagopy.kmp.habittracker.data.local.LogEntity
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitLog
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Mappers to convert between domain models and data entities.
 */

// Habit mappers
fun HabitEntity.toDomainModel(): Habit {
    return Habit(
        id = id,
        name = name,
        description = description,
        color = color,
        isActive = isActive,
        createdAt = LocalDate.parse(createdAt),
        frequencyType = try {
            FrequencyType.valueOf(frequencyType)
        } catch (e: IllegalArgumentException) {
            FrequencyType.ONCE_DAILY
        },
        intervalMinutes = intervalMinutes,
        scheduledTimes = parseScheduledTimes(scheduledTimes),
        endTime = endTime?.let { parseTime(it) }
    )
}

fun Habit.toEntity(): HabitEntity {
    return HabitEntity(
        id = id,
        name = name,
        description = description,
        color = color,
        isActive = isActive,
        createdAt = createdAt.toString(),
        frequencyType = frequencyType.name,
        intervalMinutes = intervalMinutes,
        scheduledTimes = formatScheduledTimes(scheduledTimes),
        endTime = endTime?.let { formatTime(it) }
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