package com.nagopy.kmp.habittracker.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Domain entity representing a specific instance of a habit with a scheduled time.
 * This represents a task that needs to be completed at a specific time on a specific date.
 * 
 * The distinction is:
 * - Habit: The rule/template (e.g., "Drink water every hour")
 * - Task: The specific instance (e.g., "Drink water at 10:00 AM on 2024-01-20")
 */
data class Task(
    val habitId: Long,
    val habitName: String,
    val habitDescription: String = "",
    val habitColor: String = "#2196F3",
    val date: LocalDate,
    val scheduledTime: LocalTime,
    val isCompleted: Boolean = false
)