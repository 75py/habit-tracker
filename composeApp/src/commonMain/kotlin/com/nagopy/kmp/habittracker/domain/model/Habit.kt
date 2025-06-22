package com.nagopy.kmp.habittracker.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Sealed interface representing different types of habits based on their frequency.
 * This design ensures type safety and eliminates invalid field combinations.
 */
sealed interface Habit {
    val id: Long
    val name: String
    val description: String
    val color: String
    val isActive: Boolean
    val createdAt: LocalDate
    val frequencyType: FrequencyType
    val intervalMinutes: Int
    
    // For backwards compatibility - delegate to specific implementations
    val scheduledTimes: List<LocalTime>
    val startTime: LocalTime?
    val endTime: LocalTime?
    
    /**
     * Creates a copy of this habit with modified properties.
     * This provides backwards compatibility with the data class copy() method.
     */
    fun copy(
        id: Long = this.id,
        name: String = this.name,
        description: String = this.description,
        color: String = this.color,
        isActive: Boolean = this.isActive,
        createdAt: LocalDate = this.createdAt,
        frequencyType: FrequencyType = this.frequencyType,
        intervalMinutes: Int = this.intervalMinutes,
        scheduledTimes: List<LocalTime> = this.scheduledTimes,
        startTime: LocalTime? = this.startTime,
        endTime: LocalTime? = this.endTime
    ): Habit {
        return Habit(
            id = id,
            name = name,
            description = description,
            color = color,
            isActive = isActive,
            createdAt = createdAt,
            frequencyType = frequencyType,
            intervalMinutes = intervalMinutes,
            scheduledTimes = scheduledTimes,
            startTime = startTime,
            endTime = endTime
        )
    }
}

/**
 * Habit that occurs once daily at specific scheduled times.
 */
data class DailyHabit(
    override val id: Long = 0,
    override val name: String,
    override val description: String = "",
    override val color: String = "#2196F3",
    override val isActive: Boolean = true,
    override val createdAt: LocalDate,
    override val scheduledTimes: List<LocalTime> = listOf(LocalTime(9, 0))
) : Habit {
    override val frequencyType: FrequencyType = FrequencyType.ONCE_DAILY
    override val intervalMinutes: Int = HabitIntervalValidator.VALID_ONCE_DAILY_MINUTES
    override val startTime: LocalTime? = null
    override val endTime: LocalTime? = null
}

/**
 * Habit that occurs every N hours within a time range.
 */
data class HourlyHabit(
    override val id: Long = 0,
    override val name: String,
    override val description: String = "",
    override val color: String = "#2196F3",
    override val isActive: Boolean = true,
    override val createdAt: LocalDate,
    override val intervalMinutes: Int = 60,
    override val startTime: LocalTime? = LocalTime(9, 0),
    override val endTime: LocalTime? = null,
    override val scheduledTimes: List<LocalTime> = emptyList()
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
    override val id: Long = 0,
    override val name: String,
    override val description: String = "",
    override val color: String = "#2196F3",
    override val isActive: Boolean = true,
    override val createdAt: LocalDate,
    override val intervalMinutes: Int = 60,
    override val startTime: LocalTime? = LocalTime(9, 0),
    override val endTime: LocalTime? = null,
    override val scheduledTimes: List<LocalTime> = emptyList()
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
 * Factory function for creating Habit instances for backwards compatibility
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
    // Validate parameters for backwards compatibility  
    require(HabitIntervalValidator.isValidIntervalMinutes(frequencyType, intervalMinutes)) {
        when (frequencyType) {
            FrequencyType.ONCE_DAILY -> {
                "ONCE_DAILY frequency type requires intervalMinutes to be exactly 1440 (24 hours). Got: $intervalMinutes"
            }
            FrequencyType.HOURLY -> {
                "HOURLY frequency type requires intervalMinutes to be a multiple of 60. Got: $intervalMinutes"
            }
            FrequencyType.INTERVAL -> {
                "INTERVAL frequency type requires intervalMinutes to be a divisor of 60. " +
                "Valid values: ${HabitIntervalValidator.VALID_INTERVAL_MINUTES}, got: $intervalMinutes"
            }
        }
    }
    
    return when (frequencyType) {
        FrequencyType.ONCE_DAILY -> DailyHabit(
            id = id,
            name = name,
            description = description,
            color = color,
            isActive = isActive,
            createdAt = createdAt,
            scheduledTimes = scheduledTimes
        )
        FrequencyType.HOURLY -> HourlyHabit(
            id = id,
            name = name,
            description = description,
            color = color,
            isActive = isActive,
            createdAt = createdAt,
            intervalMinutes = intervalMinutes,
            startTime = startTime,
            endTime = endTime,
            scheduledTimes = scheduledTimes
        )
        FrequencyType.INTERVAL -> IntervalHabit(
            id = id,
            name = name,
            description = description,
            color = color,
            isActive = isActive,
            createdAt = createdAt,
            intervalMinutes = intervalMinutes,
            startTime = startTime,
            endTime = endTime,
            scheduledTimes = scheduledTimes
        )
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