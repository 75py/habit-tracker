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
    
    data class HourlyHabitDetail(
        val intervalMinutes: Int = 60, // Default to 1 hour
        val startTime: LocalTime = LocalTime(9, 0), // Start time for hourly habits
        val endTime: LocalTime? = null // Optional end time for hourly habits
    ) : HabitDetail {
        init {
            require(HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.HOURLY, intervalMinutes)) {
                "HOURLY frequency type requires intervalMinutes to be a multiple of 60. Got: $intervalMinutes"
            }
        }
    }
    
    data class IntervalHabitDetail(
        val intervalMinutes: Int = 60, // Custom interval in minutes
        val startTime: LocalTime = LocalTime(9, 0), // Start time for interval habits
        val endTime: LocalTime? = null // Optional end time for interval habits
    ) : HabitDetail {
        init {
            require(HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.INTERVAL, intervalMinutes)) {
                "INTERVAL frequency type requires intervalMinutes to be a divisor of 60. " +
                "Valid values: ${HabitIntervalValidator.VALID_INTERVAL_MINUTES}, got: $intervalMinutes"
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
        is HabitDetail.HourlyHabitDetail -> FrequencyType.HOURLY
        is HabitDetail.IntervalHabitDetail -> FrequencyType.INTERVAL
    }

/**
 * Convenience extension property to get the frequency type from habit
 */
val Habit.frequencyType: FrequencyType
    get() = detail.frequencyType

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