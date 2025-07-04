package com.nagopy.kmp.habittracker.integration

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.model.HabitDetail
import com.nagopy.kmp.habittracker.domain.usecase.GetTodayTasksUseCase
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.nagopy.kmp.habittracker.domain.model.frequencyType

/**
 * Integration test demonstrating the new multiple daily tasks functionality.
 * This test shows how different habit frequencies generate different task schedules.
 */
class MultipleTasksIntegrationTest {

    @Test
    fun `should generate different task schedules for different habit frequencies`() = runTest {
        // Given - A fixed date for deterministic testing
        val testDate = LocalDate.parse("2024-01-20")
        val fixedInstant = testDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        
        // Create habits with different frequencies
        val onceDaily = Habit(
            id = 1L,
            name = "Morning Exercise",
            description = "30 minutes workout",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.OnceDailyHabitDetail(
                scheduledTimes = listOf(LocalTime(7, 0)) // Scheduled for 7 AM
            )
        )
        
        val drinkWaterInterval = Habit(
            id = 2L,
            name = "Drink Water",
            description = "Stay hydrated",
            color = "#2196F3",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.IntervalHabitDetail(
                intervalMinutes = 60, // Every hour
                startTime = LocalTime(9, 0) // Start at 9 AM
            )
        )
        
        val readingInterval = Habit(
            id = 3L,
            name = "Read",
            description = "Reading sessions",
            color = "#4CAF50",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.IntervalHabitDetail(
                intervalMinutes = 30, // Every 30 minutes (valid divisor of 60)
                startTime = LocalTime(8, 0) // Start at 8 AM
            )
        )
        
        val habits = listOf(onceDaily, drinkWaterInterval, readingInterval)
        
        // Mock repository
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getActiveHabits() } returns flowOf(habits)
        coEvery { mockRepository.getHabitLog(any(), any()) } returns null // No completed logs
        
        val useCase = GetTodayTasksUseCase(mockRepository, fixedClock)

        // When - Generate today's tasks
        val tasks = useCase().first()

        // Then - Verify the correct number of tasks are generated
        val exerciseTasks = tasks.filter { it.habitName == "Morning Exercise" }
        val waterTasks = tasks.filter { it.habitName == "Drink Water" }
        val readingTasks = tasks.filter { it.habitName == "Read" }
        
        // Exercise: Once daily - should have 1 task
        assertEquals(1, exerciseTasks.size)
        assertEquals(LocalTime(7, 0), exerciseTasks[0].scheduledTime)
        
        // Water: Hourly interval - should have many tasks (9 AM to 11 PM = 15 hours)
        assertTrue(waterTasks.size >= 15, "Expected at least 15 water tasks, got ${waterTasks.size}")
        assertEquals(LocalTime(9, 0), waterTasks[0].scheduledTime)
        assertEquals(LocalTime(10, 0), waterTasks[1].scheduledTime)
        
        // Reading: Every 30 minutes - should have many tasks (8:00, 8:30, 9:00, 9:30, etc.)
        assertTrue(readingTasks.size >= 20, "Expected at least 20 reading tasks for 30-minute intervals, got ${readingTasks.size}")
        assertEquals(LocalTime(8, 0), readingTasks[0].scheduledTime)
        if (readingTasks.size > 1) {
            assertEquals(LocalTime(8, 30), readingTasks[1].scheduledTime)
        }
        if (readingTasks.size > 2) {
            assertEquals(LocalTime(9, 0), readingTasks[2].scheduledTime)
        }
        
        // Verify all tasks are for the correct date
        tasks.forEach { task ->
            assertEquals(testDate, task.date)
            assertEquals(false, task.isCompleted) // No logs, so not completed
        }
        
        // Verify tasks are sorted by time
        for (i in 1 until tasks.size) {
            assertTrue(
                tasks[i-1].scheduledTime <= tasks[i].scheduledTime,
                "Tasks should be sorted by time: ${tasks[i-1].scheduledTime} should be <= ${tasks[i].scheduledTime}"
            )
        }
        
        println("Generated ${tasks.size} tasks for ${habits.size} habits:")
        tasks.forEach { task ->
            println("- ${task.scheduledTime}: ${task.habitName}")
        }
    }
}