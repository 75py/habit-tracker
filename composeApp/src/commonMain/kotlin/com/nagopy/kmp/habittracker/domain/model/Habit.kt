package com.nagopy.kmp.habittracker.domain.model

import kotlinx.datetime.LocalDate

/**
 * Domain entity representing a habit that a user wants to track.
 */
data class Habit(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val color: String = "#2196F3", // Default blue color
    val isActive: Boolean = true,
    val createdAt: LocalDate
)