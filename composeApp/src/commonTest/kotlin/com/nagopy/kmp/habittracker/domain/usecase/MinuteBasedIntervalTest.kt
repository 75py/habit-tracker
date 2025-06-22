package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
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

class MinuteBasedIntervalTest {

    @Test
    fun `generateIntervalTasks should create tasks every 30 minutes`() = runTest {
        // Given - A fixed date for deterministic testing
        val testDate = LocalDate.parse("2024-01-20")
        val fixedInstant = testDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        
        val stretchHabit = Habit(
            id = 1L,
            name = "Stretch",
            description = "Stretch every 30 minutes",
            color = "#4CAF50",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 30, // Every 30 minutes
            scheduledTimes = listOf(LocalTime(9, 0)) // Start at 9:00 AM
        )
        
        // Mock repository
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getActiveHabits() } returns flowOf(listOf(stretchHabit))
        coEvery { mockRepository.getHabitLog(any(), any()) } returns null
        
        val useCase = GetTodayTasksUseCase(mockRepository, fixedClock)

        // When
        val result = useCase().first()

        // Then
        val stretchTasks = result.filter { it.habitName == "Stretch" }
        
        // Should have tasks at: 9:00, 9:30, 10:00, 10:30, ... up to 23:30
        // From 9:00 to 23:30 = 14.5 hours = 29 intervals of 30 minutes + start time = 30 tasks
        assertTrue(stretchTasks.size >= 29, "Expected at least 29 stretch tasks, got ${stretchTasks.size}")
        
        // Check first few tasks have correct timing
        assertEquals(LocalTime(9, 0), stretchTasks[0].scheduledTime)
        assertEquals(LocalTime(9, 30), stretchTasks[1].scheduledTime)
        assertEquals(LocalTime(10, 0), stretchTasks[2].scheduledTime)
        assertEquals(LocalTime(10, 30), stretchTasks[3].scheduledTime)
        
        // Verify all tasks are for the correct date and habit
        stretchTasks.forEach { task ->
            assertEquals(testDate, task.date)
            assertEquals("Stretch", task.habitName)
            assertEquals(1L, task.habitId)
        }
    }
    
    @Test
    fun `generateIntervalTasks should create tasks every 20 minutes`() = runTest {
        // Given
        val testDate = LocalDate.parse("2024-01-20")
        val fixedInstant = testDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        
        val readingHabit = Habit(
            id = 2L,
            name = "Reading",
            description = "Read every 20 minutes",
            color = "#2196F3",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 20, // Every 20 minutes (valid divisor of 60)
            scheduledTimes = emptyList(), // For INTERVAL, use startTime instead
            startTime = LocalTime(8, 15) // Start at 8:15 AM
        )
        
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getActiveHabits() } returns flowOf(listOf(readingHabit))
        coEvery { mockRepository.getHabitLog(any(), any()) } returns null
        
        val useCase = GetTodayTasksUseCase(mockRepository, fixedClock)

        // When
        val result = useCase().first()

        // Then
        val readingTasks = result.filter { it.habitName == "Reading" }
        
        // Should have tasks at: 8:15, 8:35, 8:55, 9:15, 9:35, etc.
        assertTrue(readingTasks.size >= 10, "Expected at least 10 reading tasks, got ${readingTasks.size}")
        
        // Check specific timings
        assertEquals(LocalTime(8, 15), readingTasks[0].scheduledTime)
        assertEquals(LocalTime(8, 35), readingTasks[1].scheduledTime)
        assertEquals(LocalTime(8, 55), readingTasks[2].scheduledTime)
        assertEquals(LocalTime(9, 15), readingTasks[3].scheduledTime)
    }
}