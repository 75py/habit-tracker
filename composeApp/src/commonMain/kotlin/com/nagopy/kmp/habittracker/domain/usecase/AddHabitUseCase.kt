package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository

/**
 * Use case to add a new habit to the repository.
 * This encapsulates the business logic for creating habits.
 */
class AddHabitUseCase(
    private val habitRepository: HabitRepository
) {
    
    /**
     * Executes the use case to add a new habit.
     * @param habit The habit to be added
     * @return The ID of the newly created habit
     */
    suspend operator fun invoke(habit: Habit): Long {
        return habitRepository.createHabit(habit)
    }
}