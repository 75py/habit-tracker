package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository

/**
 * Use case to get a specific habit by ID.
 * This encapsulates the business logic for retrieving individual habits.
 */
class GetHabitUseCase(
    private val habitRepository: HabitRepository
) {
    
    /**
     * Executes the use case to get a habit by ID.
     * @param habitId The ID of the habit to retrieve
     * @return The habit if found, null otherwise
     */
    suspend operator fun invoke(habitId: Long): Habit? {
        return habitRepository.getHabit(habitId)
    }
}