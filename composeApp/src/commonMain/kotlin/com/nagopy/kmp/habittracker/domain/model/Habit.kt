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
    val frequencyType: FrequencyType = FrequencyType.ONCE_DAILY,
    val intervalMinutes: Int = 1440, // For hourly/interval-based habits (default 24 hours = 1440 minutes)
    val scheduledTimes: List<LocalTime> = listOf(LocalTime(9, 0)), // Default 9:00 AM
    val endTime: LocalTime? = null // End time for interval-based habits
) {
    init {
        // Validate that INTERVAL type habits have valid interval minutes
        if (frequencyType == FrequencyType.INTERVAL) {
            require(HabitIntervalValidator.isValidIntervalMinutes(intervalMinutes)) {
                "INTERVAL frequency type requires intervalMinutes to be a divisor of 60. " +
                "Valid values: ${HabitIntervalValidator.VALID_INTERVAL_MINUTES}, got: $intervalMinutes"
            }
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
     * Check if the interval minutes is valid for INTERVAL frequency type
     */
    fun isValidIntervalMinutes(intervalMinutes: Int): Boolean {
        return intervalMinutes in VALID_INTERVAL_MINUTES
    }
    
    /**
     * Get the closest valid interval minutes for the given value
     */
    fun getClosestValidIntervalMinutes(intervalMinutes: Int): Int {
        if (intervalMinutes <= 0) return VALID_INTERVAL_MINUTES.first()
        
        return VALID_INTERVAL_MINUTES.minByOrNull { kotlin.math.abs(it - intervalMinutes) }
            ?: VALID_INTERVAL_MINUTES.first()
    }
}