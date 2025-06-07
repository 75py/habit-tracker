package com.nagopy.kmp.habittracker.domain.model

import kotlinx.datetime.LocalDate

/**
 * Domain entity representing a completion log entry for a habit.
 */
data class HabitLog(
    val id: Long = 0,
    val habitId: Long,
    val date: LocalDate,
    val isCompleted: Boolean = true
)