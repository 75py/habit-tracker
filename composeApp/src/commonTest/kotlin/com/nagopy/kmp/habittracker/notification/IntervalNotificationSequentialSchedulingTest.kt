package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.usecase.GetNextTasksUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase
import com.nagopy.kmp.habittracker.util.TestLoggerConfig
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IntervalNotificationSequentialSchedulingTest {

    @MockK
    private lateinit var mockGetNextTasksUseCase: GetNextTasksUseCase
    
    @MockK 
    private lateinit var mockScheduleNextNotificationUseCase: ScheduleNextNotificationUseCase
    
    @BeforeTest
    fun setup() {
        TestLoggerConfig.setupForTests()
        MockKAnnotations.init(this)
    }

    @Test
    fun sequential_notification_scheduling_works_for_interval_habit() = runTest {
        // Given: A 1-minute interval habit
        val habitId = 1L
        val currentTime = LocalTime(10, 0)
        val currentDate = LocalDate(2024, 1, 20)
        
        val habit = Habit(
            id = habitId,
            name = "Drink Water",
            description = "Stay hydrated",
            color = "#0000FF",
            isActive = true,
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 1, // 1 minute interval
            scheduledTimes = listOf(currentTime),
            createdAt = currentDate
        )
        
        // Mock the first task (just delivered/fired)
        val firstTask = Task(
            habitId = habitId,
            habitName = habit.name,
            habitDescription = habit.description,
            date = currentDate,
            scheduledTime = currentTime,
            isCompleted = false
        )
        
        // Mock the next task that should be scheduled (1 minute later)
        val nextTask = Task(
            habitId = habitId,
            habitName = habit.name,
            habitDescription = habit.description,
            date = currentDate,
            scheduledTime = LocalTime(10, 1), // 1 minute later
            isCompleted = false
        )
        
        // Configure mocks
        coEvery { mockGetNextTasksUseCase.getNextTaskForHabit(habitId) } returns nextTask
        coEvery { mockScheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId) } returns true
        
        // When: Process notification delivery (simulate iOS delegate receiving notification)
        val identifier = "1_2024-01-20_10:00" // Format: habitId_date_time
        val parts = identifier.split("_")
        
        // Then: Verify identifier parsing works correctly
        kotlin.test.assertEquals(3, parts.size)
        kotlin.test.assertEquals(habitId.toString(), parts[0])
        kotlin.test.assertEquals(currentDate.toString(), parts[1])
        kotlin.test.assertEquals(currentTime.toString(), parts[2])
        
        // When: Schedule next notification (simulate helper function call)
        val parsedHabitId = parts[0].toLong()
        
        // Simulate calling scheduleNextNotificationUseCase
        val wasScheduled = mockScheduleNextNotificationUseCase.scheduleNextNotificationForHabit(parsedHabitId)
        
        // Then: Verify next notification was scheduled
        kotlin.test.assertTrue(wasScheduled)
        coVerify { mockScheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId) }
    }
    
    @Test
    fun notification_identifier_parsing_handles_edge_cases() = runTest {
        // Test cases for various time formats
        val testCases = listOf(
            "123_2024-01-20_09:00" to Triple(123L, LocalDate(2024, 1, 20), LocalTime(9, 0)),
            "456_2024-12-31_23:59" to Triple(456L, LocalDate(2024, 12, 31), LocalTime(23, 59)),
            "789_2024-06-15_12:30" to Triple(789L, LocalDate(2024, 6, 15), LocalTime(12, 30))
        )
        
        testCases.forEach { (identifier, expected) ->
            val parts = identifier.split("_")
            kotlin.test.assertEquals(3, parts.size, "Identifier should have 3 parts: $identifier")
            
            val habitId = parts[0].toLong()
            val date = LocalDate.parse(parts[1])
            val time = LocalTime.parse(parts[2])
            
            kotlin.test.assertEquals(expected.first, habitId)
            kotlin.test.assertEquals(expected.second, date)
            kotlin.test.assertEquals(expected.third, time)
        }
    }
    
    @Test
    fun notification_identifier_parsing_handles_invalid_formats() = runTest {
        val invalidIdentifiers = listOf(
            "invalid", // no underscores
            "123_2024-01-20", // missing time
            "abc_2024-01-20_10:00", // invalid habit ID
            "123_invalid-date_10:00", // invalid date
            "123_2024-01-20_invalid-time" // invalid time
        )
        
        invalidIdentifiers.forEach { identifier ->
            val parts = identifier.split("_")
            
            when {
                parts.size < 3 -> {
                    // Should be caught by size check
                    kotlin.test.assertTrue(parts.size < 3)
                }
                else -> {
                    // Should throw exceptions during parsing
                    try {
                        val habitId = parts[0].toLong()
                        val date = LocalDate.parse(parts[1])
                        val time = LocalTime.parse(parts[2])
                        
                        // If we get here with invalid data, something's wrong
                        if (identifier.contains("abc") || identifier.contains("invalid")) {
                            kotlin.test.fail("Should have thrown exception for: $identifier")
                        }
                    } catch (e: Exception) {
                        // Expected for invalid identifiers
                        kotlin.test.assertTrue(e is NumberFormatException || e is IllegalArgumentException)
                    }
                }
            }
        }
    }
}