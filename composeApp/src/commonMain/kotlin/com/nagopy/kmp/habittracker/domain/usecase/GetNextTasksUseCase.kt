package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitDetail
import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Use case to find the next upcoming tasks for all active habits.
 * This is used for sequential notification scheduling to find the single next task
 * that needs to be scheduled across all habits.
 */
class GetNextTasksUseCase(
    private val habitRepository: HabitRepository,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {
    
    /**
     * Finds the next upcoming task for a specific habit.
     * 
     * @param habitId The ID of the habit to find the next task for
     * @return The next upcoming task for the habit, or null if no future tasks exist
     */
    suspend fun getNextTaskForHabit(habitId: Long): Task? {
        val habit = habitRepository.getHabit(habitId) ?: return null
        if (!habit.isActive) return null
        
        val currentDateTime = clock.now().toLocalDateTime(timeZone)
        val today = currentDateTime.date
        
        // Look for tasks today and tomorrow to find the next one
        for (date in listOf(today, today.plus(DatePeriod(days = 1)))) {
            val tasks = generateTasksForHabit(habit, date)
            val nextTask = tasks.firstOrNull { task ->
                val taskDateTime = LocalDateTime(task.date, task.scheduledTime)
                taskDateTime > currentDateTime && !task.isCompleted
            }
            if (nextTask != null) {
                return nextTask
            }
        }
        
        return null
    }
    
    /**
     * Finds the next upcoming task across all active habits.
     * 
     * @return The next upcoming task across all habits, or null if no future tasks exist
     */
    suspend fun getNextUpcomingTask(): Task? {
        val habits = habitRepository.getActiveHabits().first()
        val currentDateTime = clock.now().toLocalDateTime(timeZone)
        
        var nextTask: Task? = null
        var nextTaskDateTime: LocalDateTime? = null
        
        for (habit in habits) {
            val habitNextTask = getNextTaskForHabit(habit.id)
            if (habitNextTask != null) {
                val taskDateTime = LocalDateTime(habitNextTask.date, habitNextTask.scheduledTime)
                if (nextTaskDateTime == null || taskDateTime < nextTaskDateTime) {
                    nextTask = habitNextTask
                    nextTaskDateTime = taskDateTime
                }
            }
        }
        
        return nextTask
    }
    
    /**
     * Generates task instances for a specific habit on a given date.
     * This is similar to the logic in GetTodayTasksUseCase but simplified for finding next tasks.
     */
    private suspend fun generateTasksForHabit(habit: Habit, date: LocalDate): List<Task> {
        return when (val detail = habit.detail) {
            is HabitDetail.OnceDailyHabitDetail -> {
                listOf(createTask(habit, date, detail.scheduledTime))
            }
            is HabitDetail.HourlyHabitDetail -> {
                generateHourlyTasks(habit, detail, date)
            }
            is HabitDetail.IntervalHabitDetail -> {
                generateIntervalTasks(habit, detail, date)
            }
        }
    }
    
    private suspend fun generateHourlyTasks(habit: Habit, detail: HabitDetail.HourlyHabitDetail, date: LocalDate): List<Task> {
        val tasks = mutableListOf<Task>()
        val startTime = detail.startTime
        val intervalMinutes = detail.intervalMinutes
        
        var currentTime = startTime
        val endTime = detail.endTime ?: LocalTime(23, 59)
        
        while (currentTime <= endTime) {
            tasks.add(createTask(habit, date, currentTime))
            
            val totalMinutes = currentTime.hour * 60 + currentTime.minute + intervalMinutes
            val newHour = totalMinutes / 60
            val newMinute = totalMinutes % 60
            
            if (newHour >= 24) break
            currentTime = LocalTime(newHour, newMinute)
        }
        
        return tasks
    }
    
    private suspend fun generateIntervalTasks(habit: Habit, detail: HabitDetail.IntervalHabitDetail, date: LocalDate): List<Task> {
        val tasks = mutableListOf<Task>()
        val startTime = detail.startTime
        val intervalMinutes = detail.intervalMinutes.coerceAtLeast(1)
        
        var currentTime = startTime
        val endTime = detail.endTime ?: LocalTime(23, 59)
        
        while (currentTime <= endTime) {
            tasks.add(createTask(habit, date, currentTime))
            
            val totalMinutes = currentTime.hour * 60 + currentTime.minute + intervalMinutes
            val newHour = totalMinutes / 60
            val newMinute = totalMinutes % 60
            
            if (newHour >= 24) break
            currentTime = LocalTime(newHour, newMinute)
        }
        
        return tasks
    }
    
    private suspend fun createTask(habit: Habit, date: LocalDate, time: LocalTime): Task {
        // Check completion status
        val existingLog = habitRepository.getHabitLog(habit.id, date)
        val isCompleted = when (habit.detail) {
            is HabitDetail.OnceDailyHabitDetail -> existingLog?.isCompleted == true
            // For hourly/interval habits, we assume individual completion tracking
            is HabitDetail.HourlyHabitDetail, is HabitDetail.IntervalHabitDetail -> false
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