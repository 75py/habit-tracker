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
    val scheduledTimes: List<LocalTime> = listOf(LocalTime(9, 0)), // For ONCE_DAILY: multiple times per day
    val startTime: LocalTime? = LocalTime(9, 0), // For HOURLY/INTERVAL: start time
    val endTime: LocalTime? = null // For HOURLY/INTERVAL: end time
) {
    init {
        // Validate interval minutes based on frequency type
        require(HabitIntervalValidator.isValidIntervalMinutes(frequencyType, intervalMinutes)) {
            when (frequencyType) {
                FrequencyType.ONCE_DAILY -> {
                    "ONCE_DAILY frequency type requires intervalMinutes to be exactly 1440 (24 hours). Got: $intervalMinutes"
                }
                FrequencyType.INTERVAL -> {
                    "INTERVAL frequency type requires intervalMinutes to be a valid interval. " +
                    "Valid values: ${HabitIntervalValidator.VALID_INTERVAL_MINUTES}, got: $intervalMinutes"
                }
            }
        }
    }
}

/**
 * Enum representing different frequency types for habits.
 */
enum class FrequencyType {
    ONCE_DAILY,    // Once per day at specific time(s)
    INTERVAL       // Custom interval in minutes
}

/**
 * Validation utilities for habit interval minutes
 */
object HabitIntervalValidator {
    /**
     * Valid interval minutes for INTERVAL frequency type.
     * Extended to include common hourly intervals: divisors of 60 and multiples of 60 up to 12 hours
     */
    val VALID_INTERVAL_MINUTES = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720)
    
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
            FrequencyType.INTERVAL -> intervalMinutes in VALID_INTERVAL_MINUTES
        }
    }
    
    /**
     * Get the closest valid interval minutes for the given frequency type and value
     */
    fun getClosestValidIntervalMinutes(frequencyType: FrequencyType, intervalMinutes: Int): Int {
        return when (frequencyType) {
            FrequencyType.ONCE_DAILY -> VALID_ONCE_DAILY_MINUTES
            FrequencyType.INTERVAL -> {
                if (intervalMinutes <= 0) return VALID_INTERVAL_MINUTES.first()
                
                return VALID_INTERVAL_MINUTES.minByOrNull { kotlin.math.abs(it - intervalMinutes) }
                    ?: VALID_INTERVAL_MINUTES.first()
            }
        }
    }
}