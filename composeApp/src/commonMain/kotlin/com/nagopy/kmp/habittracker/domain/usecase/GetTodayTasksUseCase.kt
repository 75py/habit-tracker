package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitDetail
import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Use case to retrieve today's tasks from all active habits.
 * Generates task instances based on each habit's schedule for the current day.
 */
class GetTodayTasksUseCase(
    private val habitRepository: HabitRepository,
    private val clock: Clock = Clock.System
) {
    
    /**
     * Executes the use case to get today's tasks.
     * @return Flow of list of task instances for today
     */
    operator fun invoke(): Flow<List<Task>> {
        val today = clock.todayIn(TimeZone.currentSystemDefault())
        return habitRepository.getActiveHabits().map { habits ->
            habits.flatMap { habit -> generateTasksForHabit(habit, today) }
                .sortedBy { it.scheduledTime }
        }
    }
    
    /**
     * Generates task instances for a specific habit on a given date.
     */
    private suspend fun generateTasksForHabit(habit: Habit, date: LocalDate): List<Task> {
        return when (habit) {
            is DailyHabit -> {
                habit.scheduledTimes.map { time ->
                    createTask(habit, date, time)
                }
            }
            is HourlyHabit -> {
                generateHourlyTasks(habit, date)
            }
            is IntervalHabit -> {
                generateIntervalTasks(habit, date)
            }
        }
    }
    
    private suspend fun generateHourlyTasks(habit: HourlyHabit, date: LocalDate): List<Task> {
        val tasks = mutableListOf<Task>()
        val startTime = habit.startTime
        val intervalMinutes = 60 // Hourly = every 60 minutes
        
        var currentTime = startTime
        val endTime = habit.endTime ?: LocalTime(23, 59) // Use end time if set, otherwise end of day
        
        while (currentTime <= endTime) {
            tasks.add(createTask(habit, date, currentTime))
            
            // Calculate next time by adding 60 minutes
            val totalMinutes = currentTime.hour * 60 + currentTime.minute + intervalMinutes
            val newHour = totalMinutes / 60
            val newMinute = totalMinutes % 60
            
            if (newHour >= 24) break
            currentTime = LocalTime(newHour, newMinute)
        }
        
        return tasks
    }
    
    private suspend fun generateIntervalTasks(habit: IntervalHabit, date: LocalDate): List<Task> {
        val tasks = mutableListOf<Task>()
        val startTime = habit.startTime
        val intervalMinutes = habit.intervalMinutes.coerceAtLeast(1)
        
        var currentTime = startTime
        val endTime = habit.endTime ?: LocalTime(23, 59) // Use end time if set, otherwise end of day
        
        while (currentTime <= endTime) {
            tasks.add(createTask(habit, date, currentTime))
            
            // Calculate next time by adding interval minutes
            val totalMinutes = currentTime.hour * 60 + currentTime.minute + intervalMinutes
            val newHour = totalMinutes / 60
            val newMinute = totalMinutes % 60
            
            if (newHour >= 24) break
            currentTime = LocalTime(newHour, newMinute)
        }
        
        return tasks
    }
    
    private suspend fun createTask(habit: Habit, date: LocalDate, time: LocalTime): Task {
        // For now, we check if the habit has any completion for the day
        // Future enhancement: track completion per time slot
        val existingLog = habitRepository.getHabitLog(habit.id, date)
        val isCompleted = when (habit) {
            is DailyHabit -> existingLog?.isCompleted == true
            // For hourly/interval habits, we assume individual completion tracking
            // would require enhancement to the data layer
            is HourlyHabit, is IntervalHabit -> false
        }
        
        return Task(
            habitId = habit.id,
            habitName = habit.name,
            habitDescription = habit.description,
            habitColor = habit.color,
            date = date,
            scheduledTime = time,
            isCompleted = isCompleted
        )
    }
}