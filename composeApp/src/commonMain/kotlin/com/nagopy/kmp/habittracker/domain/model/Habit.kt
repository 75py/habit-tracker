package com.nagopy.kmp.habittracker.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Common properties shared by all habit types.
 */
data class HabitBase(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val color: String = "#2196F3",
    val isActive: Boolean = true,
    val createdAt: LocalDate
)

/**
 * Sealed interface representing different types of habits based on their frequency.
 * This design ensures type safety and eliminates invalid field combinations.
 */
sealed interface Habit {
    val base: HabitBase
    val frequencyType: FrequencyType
    
    // Convenience accessors for common properties
    val id: Long get() = base.id
    val name: String get() = base.name
    val description: String get() = base.description
    val color: String get() = base.color
    val isActive: Boolean get() = base.isActive
    val createdAt: LocalDate get() = base.createdAt
}

/**
 * Habit that occurs once daily at specific scheduled times.
 */
data class DailyHabit(
    override val base: HabitBase,
    val scheduledTimes: List<LocalTime> = listOf(LocalTime(9, 0))
) : Habit {
    override val frequencyType: FrequencyType = FrequencyType.ONCE_DAILY
}

/**
 * Habit that occurs every N hours within a time range.
 */
data class HourlyHabit(
    override val base: HabitBase,
    val intervalMinutes: Int = 60,
    val startTime: LocalTime = LocalTime(9, 0),
    val endTime: LocalTime? = null
) : Habit {
    override val frequencyType: FrequencyType = FrequencyType.HOURLY
    
    init {
        require(HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.HOURLY, intervalMinutes)) {
            "HOURLY frequency type requires intervalMinutes to be a multiple of 60. Got: $intervalMinutes"
        }
    }
}

/**
 * Habit that occurs at custom intervals (divisors of 60 minutes).
 */
data class IntervalHabit(
    override val base: HabitBase,
    val intervalMinutes: Int = 60,
    val startTime: LocalTime = LocalTime(9, 0),
    val endTime: LocalTime? = null
) : Habit {
    override val frequencyType: FrequencyType = FrequencyType.INTERVAL
    
    init {
        require(HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.INTERVAL, intervalMinutes)) {
            "INTERVAL frequency type requires intervalMinutes to be a divisor of 60. " +
            "Valid values: ${HabitIntervalValidator.VALID_INTERVAL_MINUTES}, got: $intervalMinutes"
        }
    }
}

/**
 * Enum representing different frequency types for habits.
 */
enum class FrequencyType {
    ONCE_DAILY,    // Once per day at specific time(s)
    HOURLY,        // Every N hours starting from first scheduled time
    INTERVAL       // Custom interval in hours
}

/**
 * Validation utilities for habit interval minutes
 */
object HabitIntervalValidator {
    /**
     * Valid interval minutes for INTERVAL frequency type.
     * These are the divisors of 60: 1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60
     */
    val VALID_INTERVAL_MINUTES = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60)
    
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
            FrequencyType.HOURLY -> intervalMinutes > 0 && intervalMinutes % 60 == 0
            FrequencyType.INTERVAL -> intervalMinutes in VALID_INTERVAL_MINUTES
        }
    }
    
    /**
     * Get the closest valid interval minutes for the given frequency type and value
     */
    fun getClosestValidIntervalMinutes(frequencyType: FrequencyType, intervalMinutes: Int): Int {
        return when (frequencyType) {
            FrequencyType.ONCE_DAILY -> VALID_ONCE_DAILY_MINUTES
            FrequencyType.HOURLY -> {
                if (intervalMinutes <= 0) {
                    60 // Default to 1 hour
                } else {
                    // Round to nearest hour
                    val hours = (intervalMinutes + 30) / 60 // Add 30 for rounding
                    kotlin.math.max(1, hours) * 60
                }
            }
            FrequencyType.INTERVAL -> {
                if (intervalMinutes <= 0) return VALID_INTERVAL_MINUTES.first()
                
                return VALID_INTERVAL_MINUTES.minByOrNull { kotlin.math.abs(it - intervalMinutes) }
                    ?: VALID_INTERVAL_MINUTES.first()
            }
        }
    }
}