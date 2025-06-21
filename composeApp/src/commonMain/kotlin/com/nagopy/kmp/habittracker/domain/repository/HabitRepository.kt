package com.nagopy.kmp.habittracker.domain.repository

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Repository interface for habit data operations.
 * This interface defines the contract for data access without implementation details.
 */
interface HabitRepository {
    
    // Habit operations
    suspend fun createHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habitId: Long)
    suspend fun getHabit(habitId: Long): Habit?
    fun getAllHabits(): Flow<List<Habit>>
    fun getActiveHabits(): Flow<List<Habit>>
    
    // Habit log operations
    suspend fun addHabitLog(habitLog: HabitLog): Long
    suspend fun getHabitLog(habitId: Long, date: LocalDate): HabitLog?
}