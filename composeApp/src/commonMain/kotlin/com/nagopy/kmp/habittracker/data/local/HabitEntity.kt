package com.nagopy.kmp.habittracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a habit in the database.
 */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val color: String,
    val isActive: Boolean,
    val createdAt: String, // Stored as ISO-8601 string
    val intervalMinutes: Int = 1440, // For interval-based habits (default 24 hours = 1440 minutes)
    val scheduledTimes: String = "09:00", // For ONCE_DAILY: comma-separated list of times in HH:mm format
    val startTime: String? = "09:00", // For INTERVAL: start time in HH:mm format
    val endTime: String? = null // For INTERVAL: end time in HH:mm format
)