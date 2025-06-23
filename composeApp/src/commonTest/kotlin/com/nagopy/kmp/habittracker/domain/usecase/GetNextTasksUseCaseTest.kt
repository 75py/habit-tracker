package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import com.nagopy.kmp.habittracker.domain.model.frequencyType
import com.nagopy.kmp.habittracker.domain.model.intervalMinutes
import com.nagopy.kmp.habittracker.domain.model.scheduledTimes
import com.nagopy.kmp.habittracker.domain.model.startTime
import com.nagopy.kmp.habittracker.domain.model.endTime

class GetNextTasksUseCaseTest {

    private val mockHabitRepository = mockk<HabitRepository>()
    private val mockClock = mockk<Clock>()
    
    private val getNextTasksUseCase = GetNextTasksUseCase(
        habitRepository = mockHabitRepository,
        clock = mockClock,
        timeZone = TimeZone.UTC
    )

    @Test
    fun `getNextTaskForHabit should return next upcoming task`() = runTest {
        // Given
        val habitId = 1L
        // Create a specific LocalDateTime that represents 10:30 AM on 2024-01-20
        // Convert to Instant using UTC to avoid timezone issues in test
        val currentLocalDateTime = LocalDateTime(2024, 1, 20, 10, 30)
        val currentTime = currentLocalDateTime.toInstant(TimeZone.UTC)
        val habit = Habit(
            id = habitId,
            name = "Test Habit",
            description = "Test Description",
            color = "#FF0000",
            isActive = true,
            createdAt = LocalDate(2024, 1, 1),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 1440,
            scheduledTimes = listOf(LocalTime(9, 0), LocalTime(14, 0), LocalTime(18, 0)),
            endTime = null
        )
        
        coEvery { mockClock.now() } returns currentTime
        coEvery { mockHabitRepository.getHabit(habitId) } returns habit
        coEvery { mockHabitRepository.getHabitLog(habitId, any()) } returns null

        // When
        val nextTask = getNextTasksUseCase.getNextTaskForHabit(habitId)

        // Then
        assertNotNull(nextTask)
        assertEquals(habitId, nextTask.habitId)
        assertEquals(LocalTime(14, 0), nextTask.scheduledTime) // Next task after 10:30 is 14:00
    }

    @Test
    fun `getNextTaskForHabit should return null for inactive habit`() = runTest {
        // Given
        val habitId = 1L
        val currentLocalDateTime = LocalDateTime(2024, 1, 20, 10, 30)
        val currentTime = currentLocalDateTime.toInstant(TimeZone.UTC)
        val habit = Habit(
            id = habitId,
            name = "Test Habit",
            description = "Test Description",
            color = "#FF0000",
            isActive = false, // Inactive habit
            createdAt = LocalDate(2024, 1, 1),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 1440,
            scheduledTimes = listOf(LocalTime(14, 0)),
            endTime = null
        )
        
        coEvery { mockClock.now() } returns currentTime
        coEvery { mockHabitRepository.getHabit(habitId) } returns habit

        // When
        val nextTask = getNextTasksUseCase.getNextTaskForHabit(habitId)

        // Then
        assertNull(nextTask)
    }

    @Test
    fun `getNextTaskForHabit should return null when habit not found`() = runTest {
        // Given
        val habitId = 1L
        val currentLocalDateTime = LocalDateTime(2024, 1, 20, 10, 30)
        val currentTime = currentLocalDateTime.toInstant(TimeZone.UTC)
        
        coEvery { mockClock.now() } returns currentTime
        coEvery { mockHabitRepository.getHabit(habitId) } returns null

        // When
        val nextTask = getNextTasksUseCase.getNextTaskForHabit(habitId)

        // Then
        assertNull(nextTask)
    }

    @Test
    fun `getNextUpcomingTask should return earliest task across all habits`() = runTest {
        // Given
        val currentLocalDateTime = LocalDateTime(2024, 1, 20, 10, 30)
        val currentTime = currentLocalDateTime.toInstant(TimeZone.UTC)
        val habit1 = Habit(
            id = 1L,
            name = "Habit 1",
            description = "Test Description",
            color = "#FF0000",
            isActive = true,
            createdAt = LocalDate(2024, 1, 1),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 1440,
            scheduledTimes = listOf(LocalTime(16, 0)), // 16:00
            endTime = null
        )
        val habit2 = Habit(
            id = 2L,
            name = "Habit 2",
            description = "Test Description",
            color = "#00FF00",
            isActive = true,
            createdAt = LocalDate(2024, 1, 1),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 1440,
            scheduledTimes = listOf(LocalTime(14, 0)), // 14:00 - earlier
            endTime = null
        )
        
        coEvery { mockClock.now() } returns currentTime
        coEvery { mockHabitRepository.getActiveHabits() } returns flowOf(listOf(habit1, habit2))
        coEvery { mockHabitRepository.getHabit(1L) } returns habit1
        coEvery { mockHabitRepository.getHabit(2L) } returns habit2
        coEvery { mockHabitRepository.getHabitLog(any(), any()) } returns null

        // When
        val nextTask = getNextTasksUseCase.getNextUpcomingTask()

        // Then
        assertNotNull(nextTask)
        assertEquals(2L, nextTask.habitId) // Should return habit 2 since 14:00 is earlier than 16:00
        assertEquals(LocalTime(14, 0), nextTask.scheduledTime)
    }

    @Test
    fun `getNextUpcomingTask should return null when no active habits`() = runTest {
        // Given
        val currentLocalDateTime = LocalDateTime(2024, 1, 20, 10, 30)
        val currentTime = currentLocalDateTime.toInstant(TimeZone.UTC)
        
        coEvery { mockClock.now() } returns currentTime
        coEvery { mockHabitRepository.getActiveHabits() } returns flowOf(emptyList())

        // When
        val nextTask = getNextTasksUseCase.getNextUpcomingTask()

        // Then
        assertNull(nextTask)
    }
}