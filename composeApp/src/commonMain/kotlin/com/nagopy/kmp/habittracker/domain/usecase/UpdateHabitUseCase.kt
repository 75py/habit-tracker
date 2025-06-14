package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository

/**
 * Use case to update an existing habit in the repository.
 * This encapsulates the business logic for updating habits.
 */
class UpdateHabitUseCase(
    private val habitRepository: HabitRepository,
    private val manageNotificationsUseCase: ManageNotificationsUseCase
) {
    
    /**
     * Executes the use case to update an existing habit.
     * @param habit The habit with updated data
     */
    suspend operator fun invoke(habit: Habit) {
        // Get the current habit state to check if it's being deactivated
        val currentHabit = habitRepository.getHabit(habit.id)
        
        // Update the habit
        habitRepository.updateHabit(habit)
        
        // If the habit is being deactivated, cancel its notifications
        if (currentHabit != null && currentHabit.isActive && !habit.isActive) {
            manageNotificationsUseCase.cancelHabitNotifications(habit.id)
        }
    }
}