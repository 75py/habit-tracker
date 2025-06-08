package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository

/**
 * Use case to update an existing habit in the repository.
 * This encapsulates the business logic for updating habits.
 */
class UpdateHabitUseCase(
    private val habitRepository: HabitRepository
) {
    
    /**
     * Executes the use case to update an existing habit.
     * @param habit The habit with updated data
     */
    suspend operator fun invoke(habit: Habit) {
        habitRepository.updateHabit(habit)
    }
}