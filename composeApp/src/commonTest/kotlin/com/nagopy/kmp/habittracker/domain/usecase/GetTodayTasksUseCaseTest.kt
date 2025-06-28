package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.model.HabitDetail
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
import kotlin.test.assertFalse
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
                detail = HabitDetail.OnceDailyHabitDetail(
                    scheduledTimes = listOf(LocalTime(7, 0))
                )
            ),
            Habit(
                id = 2,
                name = "Meditate",
                description = "10 minutes meditation",
                color = "#4CAF50",
                isActive = true,
                createdAt = LocalDate.parse("2024-01-03"),
                detail = HabitDetail.OnceDailyHabitDetail(
                    scheduledTimes = listOf(LocalTime(8, 30))
                )
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
            detail = HabitDetail.HourlyHabitDetail(
                startTime = LocalTime(9, 0)
            )
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
            detail = HabitDetail.HourlyHabitDetail(
                startTime = LocalTime(9, 0)
            )
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

    @Test
    fun `hourly habit with end time should only generate tasks within time window`() = runTest {
        // Given
        val fixedDate = LocalDate.parse("2024-01-20")
        val fixedInstant = fixedDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        
        val hourlyHabitWithEndTime = Habit(
            id = 1,
            name = "Drink Water",
            description = "Stay hydrated",
            color = "#2196F3",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.HourlyHabitDetail(
                startTime = LocalTime(9, 0), // Start at 9:00 AM
                endTime = LocalTime(17, 0) // End at 5:00 PM
            )
        )
        
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getActiveHabits() } returns flowOf(listOf(hourlyHabitWithEndTime))
        coEvery { mockRepository.getHabitLog(any(), any()) } returns null
        
        val useCase = GetTodayTasksUseCase(mockRepository, fixedClock)

        // When
        val result = useCase().first()

        // Then
        val expectedTasks = 9 // 9 AM to 5 PM = 9 tasks (9, 10, 11, 12, 13, 14, 15, 16, 17)
        assertEquals(expectedTasks, result.size)
        
        // Check first task is at 9:00
        assertEquals(LocalTime(9, 0), result.first().scheduledTime)
        
        // Check last task is at 17:00 (5 PM)
        assertEquals(LocalTime(17, 0), result.last().scheduledTime)
        
        // Verify no tasks beyond 17:00
        result.forEach { task ->
            assertTrue(task.scheduledTime <= LocalTime(17, 0), 
                "Task scheduled at ${task.scheduledTime} should not be beyond end time")
        }
    }

    @Test
    fun `interval habit with end time should only generate tasks within time window`() = runTest {
        // Given
        val fixedDate = LocalDate.parse("2024-01-20")
        val fixedInstant = fixedDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        
        val intervalHabitWithEndTime = Habit(
            id = 1,
            name = "Take Vitamins",
            description = "Every 30 minutes",
            color = "#9C27B0",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.IntervalHabitDetail(
                intervalMinutes = 30, // 30 minutes (valid divisor of 60)
                startTime = LocalTime(8, 0), // Start at 8:00 AM
                endTime = LocalTime(20, 0) // End at 8:00 PM
            )
        )
        
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getActiveHabits() } returns flowOf(listOf(intervalHabitWithEndTime))
        coEvery { mockRepository.getHabitLog(any(), any()) } returns null
        
        val useCase = GetTodayTasksUseCase(mockRepository, fixedClock)

        // When
        val result = useCase().first()

        // Then - With 30-minute intervals from 8:00 to 20:00, we should have many tasks
        // First few expected times: 8:00, 8:30, 9:00, 9:30, ..., 19:30, 20:00
        val expectedFirstTimes = listOf(
            LocalTime(8, 0),   // Start time
            LocalTime(8, 30),  // 8:00 + 30 minutes
            LocalTime(9, 0),   // 8:30 + 30 minutes
            LocalTime(9, 30),  // 9:00 + 30 minutes
            LocalTime(10, 0)   // 9:30 + 30 minutes
        )
        
        assertTrue(result.size >= 24, "Should have many tasks for 30-minute intervals over 12 hours")
        
        // Check first few tasks
        result.take(5).forEachIndexed { index, task ->
            assertEquals(expectedFirstTimes[index], task.scheduledTime)
            assertEquals("Take Vitamins", task.habitName)
        }
        
        // Check last task is at 20:00 (end time)
        assertEquals(LocalTime(20, 0), result.last().scheduledTime)
        
        // Verify no tasks beyond 20:00
        result.forEach { task ->
            assertTrue(task.scheduledTime <= LocalTime(20, 0), 
                "Task scheduled at ${task.scheduledTime} should not be beyond end time")
        }
    }

    @Test
    fun `habit without end time should work as before (generate tasks until end of day)`() = runTest {
        // Given
        val fixedDate = LocalDate.parse("2024-01-20")
        val fixedInstant = fixedDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        
        val intervalHabitWithoutEndTime = Habit(
            id = 1,
            name = "Check Email",
            description = "Every 20 minutes",
            color = "#607D8B",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.IntervalHabitDetail(
                intervalMinutes = 20, // 20 minutes (valid divisor of 60)
                startTime = LocalTime(6, 0), // Start at 6:00 AM
                endTime = null // No end time, should generate tasks until end of day
            )
        )
        
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getActiveHabits() } returns flowOf(listOf(intervalHabitWithoutEndTime))
        coEvery { mockRepository.getHabitLog(any(), any()) } returns null
        
        val useCase = GetTodayTasksUseCase(mockRepository, fixedClock)

        // When
        val result = useCase().first()

        // Then - With 20-minute intervals from 6:00 until end of day (around 23:59)
        // First few expected times: 6:00, 6:20, 6:40, 7:00, 7:20, etc.
        val expectedFirstTimes = listOf(
            LocalTime(6, 0),   // Start time
            LocalTime(6, 20),  // 6:00 + 20 minutes
            LocalTime(6, 40),  // 6:20 + 20 minutes
            LocalTime(7, 0),   // 6:40 + 20 minutes
            LocalTime(7, 20)   // 7:00 + 20 minutes
        )
        
        assertTrue(result.size >= 50, "Should have many tasks for 20-minute intervals throughout the day")
        
        // Check first few tasks
        result.take(5).forEachIndexed { index, task ->
            assertEquals(expectedFirstTimes[index], task.scheduledTime)
            assertEquals("Check Email", task.habitName)
        }
        
        // Check last task is before end of day
        assertTrue(result.last().scheduledTime < LocalTime(23, 59), 
            "Last task should be before end of day")
    }


}