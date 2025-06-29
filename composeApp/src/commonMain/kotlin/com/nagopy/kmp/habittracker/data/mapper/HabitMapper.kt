package com.nagopy.kmp.habittracker.data.mapper

import com.nagopy.kmp.habittracker.data.local.HabitEntity
import com.nagopy.kmp.habittracker.data.local.LogEntity
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitDetail
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
        else -> FrequencyType.INTERVAL // All other intervals
    }
    
    val detail = when (detectedFrequencyType) {
        FrequencyType.ONCE_DAILY -> HabitDetail.OnceDailyHabitDetail(
            scheduledTimes = parseScheduledTimes(scheduledTimes).ifEmpty { listOf(LocalTime(9, 0)) }
        )
        FrequencyType.INTERVAL -> HabitDetail.IntervalHabitDetail(
            intervalMinutes = intervalMinutes,
            startTime = startTime?.let { parseTime(it) } ?: LocalTime(9, 0),
            endTime = endTime?.let { parseTime(it) }
        )
    }
    
    return Habit(
        id = id,
        name = name,
        description = description,
        color = color,
        isActive = isActive,
        createdAt = LocalDate.parse(createdAt),
        detail = detail
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
        intervalMinutes = when (val detail = this.detail) {
            is HabitDetail.OnceDailyHabitDetail -> HabitIntervalValidator.VALID_ONCE_DAILY_MINUTES
            is HabitDetail.IntervalHabitDetail -> detail.intervalMinutes
        },
        scheduledTimes = when (val detail = this.detail) {
            is HabitDetail.OnceDailyHabitDetail -> formatScheduledTimes(detail.scheduledTimes)
            is HabitDetail.IntervalHabitDetail -> "" // No scheduled times for interval habits
        },
        startTime = when (val detail = this.detail) {
            is HabitDetail.OnceDailyHabitDetail -> null // No start time for daily habits
            is HabitDetail.IntervalHabitDetail -> formatTime(detail.startTime)
        },
        endTime = when (val detail = this.detail) {
            is HabitDetail.OnceDailyHabitDetail -> null // No end time for daily habits
            is HabitDetail.IntervalHabitDetail -> detail.endTime?.let { formatTime(it) }
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