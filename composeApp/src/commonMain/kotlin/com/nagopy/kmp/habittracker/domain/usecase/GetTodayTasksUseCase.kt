package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to retrieve today's tasks from all active habits.
 * Returns all active habits as today's tasks to be completed.
 */
class GetTodayTasksUseCase(
    private val habitRepository: HabitRepository
) {
    
    /**
     * Executes the use case to get today's tasks.
     * @return Flow of list of active habits that represent today's tasks
     */
    operator fun invoke(): Flow<List<Habit>> {
        return habitRepository.getActiveHabits()
    }
}