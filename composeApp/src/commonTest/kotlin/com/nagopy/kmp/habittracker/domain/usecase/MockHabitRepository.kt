package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitLog
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

/**
 * Mock implementation of HabitRepository for testing use cases.
 */
class MockHabitRepository : HabitRepository {
    
    private var habits = mutableListOf<Habit>()
    private var habitLogs = mutableListOf<HabitLog>()
    private var nextHabitId = 1L
    private var nextLogId = 1L
    
    fun setHabits(habits: List<Habit>) {
        this.habits = habits.toMutableList()
    }
    
    fun setHabitLogs(logs: List<HabitLog>) {
        this.habitLogs = logs.toMutableList()
    }
    
    override suspend fun createHabit(habit: Habit): Long {
        val id = nextHabitId++
        val habitWithId = habit.copy(id = id)
        habits.add(habitWithId)
        return id
    }
    
    override suspend fun updateHabit(habit: Habit) {
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit
        }
    }
    
    override suspend fun deleteHabit(habitId: Long) {
        habits.removeAll { it.id == habitId }
    }
    
    override suspend fun getHabit(habitId: Long): Habit? {
        return habits.find { it.id == habitId }
    }
    
    override fun getAllHabits(): Flow<List<Habit>> {
        return flowOf(habits.toList())
    }
    
    override fun getActiveHabits(): Flow<List<Habit>> {
        return flowOf(habits.filter { it.isActive })
    }
    
    override suspend fun addHabitLog(habitLog: HabitLog): Long {
        val id = nextLogId++
        val logWithId = habitLog.copy(id = id)
        habitLogs.add(logWithId)
        return id
    }
    
    override suspend fun removeHabitLog(habitId: Long, date: LocalDate) {
        habitLogs.removeAll { it.habitId == habitId && it.date == date }
    }
    
    override suspend fun getHabitLog(habitId: Long, date: LocalDate): HabitLog? {
        return habitLogs.find { it.habitId == habitId && it.date == date }
    }
    
    override fun getHabitLogsForHabit(habitId: Long): Flow<List<HabitLog>> {
        return flowOf(habitLogs.filter { it.habitId == habitId })
    }
    
    override fun getHabitLogsInDateRange(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HabitLog>> {
        return flowOf(
            habitLogs.filter { 
                it.habitId == habitId && 
                it.date >= startDate && 
                it.date <= endDate 
            }
        )
    }
}