package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.HabitLog
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Use case to complete a task (mark a habit as completed for a specific date).
 * This encapsulates the business logic for marking habits as completed.
 */
class CompleteTaskUseCase(
    private val habitRepository: HabitRepository
) {
    
    /**
     * Executes the use case to complete a task for today.
     * @param habitId The ID of the habit to mark as completed
     * @return The ID of the created habit log entry
     */
    suspend operator fun invoke(habitId: Long): Long {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return invoke(habitId, today)
    }
    
    /**
     * Executes the use case to complete a task for a specific date.
     * @param habitId The ID of the habit to mark as completed
     * @param date The date for which to mark the habit as completed
     * @return The ID of the created habit log entry
     */
    suspend operator fun invoke(habitId: Long, date: LocalDate): Long {
        val habitLog = HabitLog(
            habitId = habitId,
            date = date,
            isCompleted = true
        )
        return habitRepository.addHabitLog(habitLog)
    }
}