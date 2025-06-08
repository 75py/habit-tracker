package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to retrieve all habits from the repository.
 * This provides a clean interface for the presentation layer to get all habits.
 */
class GetAllHabitsUseCase(
    private val habitRepository: HabitRepository
) {
    
    /**
     * Executes the use case to get all habits.
     * @return Flow of list of all habits
     */
    operator fun invoke(): Flow<List<Habit>> {
        return habitRepository.getAllHabits()
    }
}