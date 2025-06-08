package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.repository.HabitRepository

/**
 * Use case to delete a habit from the repository.
 * This encapsulates the business logic for deleting habits.
 */
class DeleteHabitUseCase(
    private val habitRepository: HabitRepository
) {
    
    /**
     * Executes the use case to delete a habit.
     * @param habitId The ID of the habit to be deleted
     */
    suspend operator fun invoke(habitId: Long) {
        habitRepository.deleteHabit(habitId)
    }
}