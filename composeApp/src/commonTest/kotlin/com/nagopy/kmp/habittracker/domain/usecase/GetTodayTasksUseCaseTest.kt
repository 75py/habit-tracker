package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.Task
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

class GetTodayTasksUseCaseTest {

    @Test
    fun `invoke should return task instances for active habits with once daily frequency`() = runTest {
        // Given
        val fixedDate = LocalDate.parse("2024-01-20")
        val fixedInstant = fixedDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        
        val activeHabits = listOf(
            Habit(
                id = 1,
                name = "Exercise",
                description = "Daily workout",
                color = "#FF5722",
                isActive = true,
                createdAt = LocalDate.parse("2024-01-01"),
                frequencyType = FrequencyType.ONCE_DAILY,
                scheduledTimes = listOf(LocalTime(7, 0))
            ),
            Habit(
                id = 2,
                name = "Meditate",
                description = "10 minutes meditation",
                color = "#4CAF50",
                isActive = true,
                createdAt = LocalDate.parse("2024-01-03"),
                frequencyType = FrequencyType.ONCE_DAILY,
                scheduledTimes = listOf(LocalTime(8, 30))
            )
        )
        
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getActiveHabits() } returns flowOf(activeHabits)
        coEvery { mockRepository.getHabitLog(any(), any()) } returns null
        
        val useCase = GetTodayTasksUseCase(mockRepository, fixedClock)

        // When
        val result = useCase().first()

        // Then
        assertEquals(2, result.size)
        
        val exerciseTask = result.find { it.habitName == "Exercise" }!!
        assertEquals(1L, exerciseTask.habitId)
        assertEquals("Exercise", exerciseTask.habitName)
        assertEquals("Daily workout", exerciseTask.habitDescription)
        assertEquals(LocalTime(7, 0), exerciseTask.scheduledTime)
        assertEquals(fixedDate, exerciseTask.date)
        assertEquals(false, exerciseTask.isCompleted)
        
        val meditateTask = result.find { it.habitName == "Meditate" }!!
        assertEquals(2L, meditateTask.habitId)
        assertEquals("Meditate", meditateTask.habitName)
        assertEquals(LocalTime(8, 30), meditateTask.scheduledTime)
    }

    @Test
    fun `invoke should return multiple task instances for hourly habits`() = runTest {
        // Given
        val fixedDate = LocalDate.parse("2024-01-20")
        val fixedInstant = fixedDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        
        val hourlyHabit = Habit(
            id = 1,
            name = "Drink Water",
            description = "Stay hydrated",
            color = "#2196F3",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.HOURLY,
            scheduledTimes = listOf(LocalTime(9, 0))
        )
        
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getActiveHabits() } returns flowOf(listOf(hourlyHabit))
        coEvery { mockRepository.getHabitLog(any(), any()) } returns null
        
        val useCase = GetTodayTasksUseCase(mockRepository, fixedClock)

        // When
        val result = useCase().first()

        // Then
        assertTrue(result.size >= 15) // Should have many hourly tasks (9 AM to 11 PM = 15 hours)
        
        // Check first few tasks
        val firstTask = result[0]
        assertEquals(LocalTime(9, 0), firstTask.scheduledTime)
        assertEquals("Drink Water", firstTask.habitName)
        
        val secondTask = result[1]
        assertEquals(LocalTime(10, 0), secondTask.scheduledTime)
        assertEquals("Drink Water", secondTask.habitName)
        
        // Verify tasks are sorted by time
        for (i in 1 until result.size) {
            assertTrue(result[i-1].scheduledTime <= result[i].scheduledTime)
        }
    }

    @Test
    fun `invoke should return empty list when no active habits exist`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getActiveHabits() } returns flowOf(emptyList())
        coEvery { mockRepository.getHabitLog(any(), any()) } returns null
        
        val useCase = GetTodayTasksUseCase(mockRepository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `invoke should mark hourly and interval tasks as not completed by default`() = runTest {
        // Given
        val fixedDate = LocalDate.parse("2024-01-20")
        val fixedInstant = fixedDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        
        val hourlyHabit = Habit(
            id = 1,
            name = "Drink Water",
            description = "Stay hydrated",
            color = "#2196F3",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.HOURLY,
            scheduledTimes = listOf(LocalTime(9, 0))
        )
        
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getActiveHabits() } returns flowOf(listOf(hourlyHabit))
        coEvery { mockRepository.getHabitLog(1, fixedDate) } returns null // No completion log
        
        val useCase = GetTodayTasksUseCase(mockRepository, fixedClock)

        // When
        val result = useCase().first()

        // Then - All hourly tasks should be marked as not completed
        assertTrue(result.isNotEmpty())
        result.forEach { task ->
            assertFalse(task.isCompleted, "Task at ${task.scheduledTime} should not be completed by default")
            assertEquals("Drink Water", task.habitName)
            assertEquals(fixedDate, task.date)
        }
    }


}