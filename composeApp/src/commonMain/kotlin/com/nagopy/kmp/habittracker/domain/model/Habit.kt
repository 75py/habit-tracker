package com.nagopy.kmp.habittracker.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Domain entity representing a habit that a user wants to track.
 */
data class Habit(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val color: String = "#2196F3", // Default blue color
    val isActive: Boolean = true,
    val createdAt: LocalDate,
    val detail: HabitDetail
)

sealed interface HabitDetail {
    data class OnceDailyHabitDetail(
        val scheduledTimes: List<LocalTime> = listOf(LocalTime(9, 0)) // Scheduled times per day
    ) : HabitDetail
    
    data class IntervalHabitDetail(
        val intervalMinutes: Int = 60, // Custom interval in minutes (supports both sub-hour and multi-hour)
        val startTime: LocalTime = LocalTime(9, 0), // Start time for interval habits
        val endTime: LocalTime? = null // Optional end time for interval habits
    ) : HabitDetail {
        init {
            require(HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.INTERVAL, intervalMinutes)) {
                "INTERVAL frequency type requires intervalMinutes to be valid. " +
                "Valid values: sub-hour (${HabitIntervalValidator.VALID_SUB_HOUR_INTERVAL_MINUTES}) or multi-hour multiples of 60, got: $intervalMinutes"
            }
        }
    }
}

/**
 * Convenience extension property to get the frequency type from habit detail
 */
val HabitDetail.frequencyType: FrequencyType
    get() = when (this) {
        is HabitDetail.OnceDailyHabitDetail -> FrequencyType.ONCE_DAILY
        is HabitDetail.IntervalHabitDetail -> FrequencyType.INTERVAL
    }

/**
 * Convenience extension property to get the frequency type from habit
 */
val Habit.frequencyType: FrequencyType
    get() = detail.frequencyType

/**
 * Convenience extension properties for backwards compatibility with existing code
 */
val Habit.scheduledTimes: List<LocalTime>
    get() = when (val detail = this.detail) {
        is HabitDetail.OnceDailyHabitDetail -> detail.scheduledTimes
        is HabitDetail.IntervalHabitDetail -> emptyList() // INTERVAL doesn't use scheduledTimes
    }

val Habit.intervalMinutes: Int
    get() = when (val detail = this.detail) {
        is HabitDetail.OnceDailyHabitDetail -> 1440 // 24 hours for daily habits
        is HabitDetail.IntervalHabitDetail -> detail.intervalMinutes
    }

val Habit.startTime: LocalTime?
    get() = when (val detail = this.detail) {
        is HabitDetail.OnceDailyHabitDetail -> detail.scheduledTimes.firstOrNull()
        is HabitDetail.IntervalHabitDetail -> detail.startTime
    }

/**
 * Factory function for creating Habit instances from the old structure
 * This helps with migration from the previous API
 */
fun Habit(
    id: Long = 0,
    name: String,
    description: String = "",
    color: String = "#2196F3",
    isActive: Boolean = true,
    createdAt: LocalDate,
    frequencyType: FrequencyType = FrequencyType.ONCE_DAILY,
    intervalMinutes: Int = 1440,
    scheduledTimes: List<LocalTime> = listOf(LocalTime(9, 0)),
    startTime: LocalTime? = LocalTime(9, 0),
    endTime: LocalTime? = null
): Habit {
    val detail = when (frequencyType) {
        FrequencyType.ONCE_DAILY -> HabitDetail.OnceDailyHabitDetail(
            scheduledTimes = scheduledTimes.ifEmpty { listOf(LocalTime(9, 0)) }
        )
        FrequencyType.INTERVAL -> HabitDetail.IntervalHabitDetail(
            intervalMinutes = intervalMinutes,
            startTime = startTime ?: LocalTime(9, 0),
            endTime = endTime
        )
    }
    
    return Habit(
        id = id,
        name = name,
        description = description,
        color = color,
        isActive = isActive,
        createdAt = createdAt,
        detail = detail
    )
}

val Habit.endTime: LocalTime?
    get() = when (val detail = this.detail) {
        is HabitDetail.OnceDailyHabitDetail -> null
        is HabitDetail.IntervalHabitDetail -> detail.endTime
    }

/**
 * Enum representing different frequency types for habits.
 */
enum class FrequencyType {
    ONCE_DAILY,    // Once per day at specific time(s)
    INTERVAL       // Custom interval (supports both sub-hour and multi-hour)
}

/**
 * Validation utilities for habit interval minutes
 */
object HabitIntervalValidator {
    /**
     * Valid sub-hour interval minutes for INTERVAL frequency type.
     * These are the divisors of 60: 1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60
     */
    val VALID_SUB_HOUR_INTERVAL_MINUTES = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60)
    
    /**
     * Valid interval minutes for ONCE_DAILY frequency type.
     * Only 1440 minutes (24 hours) is allowed.
     */
    const val VALID_ONCE_DAILY_MINUTES = 1440
    
    /**
     * Check if the interval minutes is valid for the given frequency type
     */
    fun isValidIntervalMinutes(frequencyType: FrequencyType, intervalMinutes: Int): Boolean {
        return when (frequencyType) {
            FrequencyType.ONCE_DAILY -> intervalMinutes == VALID_ONCE_DAILY_MINUTES
            FrequencyType.INTERVAL -> {
                // Support both sub-hour intervals (divisors of 60) and multi-hour intervals (multiples of 60)
                intervalMinutes in VALID_SUB_HOUR_INTERVAL_MINUTES || 
                (intervalMinutes > 60 && intervalMinutes % 60 == 0)
            }
        }
    }
    
    /**
     * Get the closest valid interval minutes for the given frequency type and value
     */
    fun getClosestValidIntervalMinutes(frequencyType: FrequencyType, intervalMinutes: Int): Int {
        return when (frequencyType) {
            FrequencyType.ONCE_DAILY -> VALID_ONCE_DAILY_MINUTES
            FrequencyType.INTERVAL -> {
                if (intervalMinutes <= 0) return VALID_SUB_HOUR_INTERVAL_MINUTES.first()
                
                // For intervals <= 60, use sub-hour valid values
                if (intervalMinutes <= 60) {
                    return VALID_SUB_HOUR_INTERVAL_MINUTES.minByOrNull { kotlin.math.abs(it - intervalMinutes) }
                        ?: VALID_SUB_HOUR_INTERVAL_MINUTES.first()
                }
                
                // For intervals > 60, round to nearest hour (multiples of 60)
                val hours = (intervalMinutes + 30) / 60 // Add 30 for rounding
                return kotlin.math.max(2, hours) * 60 // Minimum 2 hours for multi-hour intervals
            }
        }
    }
    
    /**
     * Check if an interval is a multi-hour interval (> 60 minutes and multiple of 60)
     */
    fun isMultiHourInterval(intervalMinutes: Int): Boolean {
        return intervalMinutes > 60 && intervalMinutes % 60 == 0
    }
    
    /**
     * Check if an interval is a sub-hour interval (<= 60 minutes and divisor of 60)
     */
    fun isSubHourInterval(intervalMinutes: Int): Boolean {
        return intervalMinutes in VALID_SUB_HOUR_INTERVAL_MINUTES
    }
}