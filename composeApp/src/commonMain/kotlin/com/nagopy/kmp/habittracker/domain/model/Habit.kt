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
        val intervalMinutes: Int = 60, // Custom interval in minutes
        val startTime: LocalTime = LocalTime(9, 0), // Start time for interval habits
        val endTime: LocalTime? = null // Optional end time for interval habits
    ) : HabitDetail {
        init {
            require(HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.INTERVAL, intervalMinutes)) {
                "INTERVAL frequency type requires intervalMinutes to be a valid interval. " +
                "Valid values: ${HabitIntervalValidator.getAllValidIntervalMinutes()}, got: $intervalMinutes"
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
 * Enum representing different frequency types for habits.
 */
enum class FrequencyType {
    ONCE_DAILY,    // Once per day at specific time(s)
    INTERVAL       // Custom interval in minutes or hours
}

/**
 * Validation utilities for habit interval minutes
 */
object HabitIntervalValidator {
    /**
     * Valid interval minutes for INTERVAL frequency type.
     * These are the divisors of 60 (for minute intervals) and multiples of 60 (for hour intervals)
     */
    val VALID_MINUTE_INTERVALS = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30)
    val VALID_HOUR_INTERVALS = listOf(60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720)
    
    /**
     * Valid interval minutes for ONCE_DAILY frequency type.
     * Only 1440 minutes (24 hours) is allowed.
     */
    const val VALID_ONCE_DAILY_MINUTES = 1440
    
    /**
     * Get all valid interval minutes for INTERVAL frequency type
     */
    fun getAllValidIntervalMinutes(): List<Int> {
        return (VALID_MINUTE_INTERVALS + VALID_HOUR_INTERVALS).sorted()
    }
    
    /**
     * Check if the interval minutes is valid for the given frequency type
     */
    fun isValidIntervalMinutes(frequencyType: FrequencyType, intervalMinutes: Int): Boolean {
        return when (frequencyType) {
            FrequencyType.ONCE_DAILY -> intervalMinutes == VALID_ONCE_DAILY_MINUTES
            FrequencyType.INTERVAL -> intervalMinutes in getAllValidIntervalMinutes()
        }
    }
    
    /**
     * Get the closest valid interval minutes for the given frequency type and value
     */
    fun getClosestValidIntervalMinutes(frequencyType: FrequencyType, intervalMinutes: Int): Int {
        return when (frequencyType) {
            FrequencyType.ONCE_DAILY -> VALID_ONCE_DAILY_MINUTES
            FrequencyType.INTERVAL -> {
                if (intervalMinutes <= 0) return getAllValidIntervalMinutes().first()
                
                return getAllValidIntervalMinutes().minByOrNull { kotlin.math.abs(it - intervalMinutes) }
                    ?: getAllValidIntervalMinutes().first()
            }
        }
    }
}