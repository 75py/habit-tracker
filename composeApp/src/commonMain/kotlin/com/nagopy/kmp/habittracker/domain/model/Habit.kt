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
    val intervalHours: Int = 24, // For hourly/interval-based habits
    val scheduledTimes: List<LocalTime> = listOf(LocalTime(9, 0)) // Default 9:00 AM
)

/**
 * Enum representing different frequency types for habits.
 */
enum class FrequencyType {
    ONCE_DAILY,    // Once per day at specific time(s)
    HOURLY,        // Every N hours starting from first scheduled time
    INTERVAL       // Custom interval in hours
}