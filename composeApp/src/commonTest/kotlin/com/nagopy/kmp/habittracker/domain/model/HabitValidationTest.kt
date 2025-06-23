package com.nagopy.kmp.habittracker.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HabitValidationTest {

    @Test
    fun `Habit creation should succeed with valid INTERVAL divisor values`() {
        val validIntervalMinutes = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60)
        
        validIntervalMinutes.forEach { intervalMinutes ->
            val habit = Habit(
                id = 1L,
                name = "Test Habit",
                description = "Test",
                createdAt = LocalDate.parse("2024-01-01"),
                detail = HabitDetail.IntervalHabitDetail(intervalMinutes = intervalMinutes)
            )
            assertEquals(intervalMinutes, habit.intervalMinutes)
        }
    }

    @Test
    fun `Habit creation should fail with invalid INTERVAL values`() {
        // Only include values that are neither divisors of 60 nor multiples of 60
        val invalidIntervalMinutes = listOf(7, 8, 9, 11, 13, 14, 16, 17, 18, 19, 21, 25, 45) // Removed 90, 120
        
        invalidIntervalMinutes.forEach { intervalMinutes ->
            val exception = assertFailsWith<IllegalArgumentException> {
                Habit(
                    id = 1L,
                    name = "Test Habit",
                    description = "Test",
                    createdAt = LocalDate.parse("2024-01-01"),
                    detail = HabitDetail.IntervalHabitDetail(intervalMinutes = intervalMinutes)
                )
            }
            assertTrue(
                exception.message!!.contains("INTERVAL frequency type requires intervalMinutes to be valid"),
                "Expected error message about unified INTERVAL validation, got: ${exception.message}"
            )
        }
    }

    @Test
    fun `Habit creation should succeed with valid ONCE_DAILY values`() {
        val habit = Habit(
            id = 1L,
            name = "Test Habit",
            description = "Test",
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.OnceDailyHabitDetail(scheduledTimes = listOf(LocalTime(9, 0)))
        )
        assertEquals(1440, habit.intervalMinutes) // Should return 1440 for compatibility
    }

    @Test
    fun `Habit creation should succeed with different ONCE_DAILY times`() {
        val times = listOf(LocalTime(6, 0), LocalTime(12, 30), LocalTime(18, 45))
        
        times.forEach { time ->
            val habit = Habit(
                id = 1L,
                name = "Test Habit",
                description = "Test",
                createdAt = LocalDate.parse("2024-01-01"),
                detail = HabitDetail.OnceDailyHabitDetail(scheduledTimes = listOf(time))
            )
            assertEquals(time, habit.startTime)
            assertEquals(FrequencyType.ONCE_DAILY, habit.frequencyType)
        }
    }

    @Test
    fun `Habit creation should succeed with valid HOURLY values`() {
        val validIntervalMinutes = listOf(60, 120, 180, 240, 300, 360, 420, 480, 540, 600)
        
        validIntervalMinutes.forEach { intervalMinutes ->
            val habit = Habit(
                id = 1L,
                name = "Test Habit",
                description = "Test",
                createdAt = LocalDate.parse("2024-01-01"),
                detail = HabitDetail.IntervalHabitDetail(intervalMinutes = intervalMinutes)
            )
            assertEquals(intervalMinutes, habit.intervalMinutes)
        }
    }

    @Test
    fun `Habit creation should fail with invalid INTERVAL values for unified type`() {
        // Only include values that are invalid for both sub-hour and multi-hour intervals
        val invalidIntervalMinutes = listOf(7, 8, 9, 11, 13, 14, 16, 17, 18, 19, 21, 25, 45, 70, 90, 110, 130, 150, 170, 190, 210, 230, 250)
        
        invalidIntervalMinutes.forEach { intervalMinutes ->
            val exception = assertFailsWith<IllegalArgumentException> {
                Habit(
                    id = 1L,
                    name = "Test Habit",
                    description = "Test",
                    createdAt = LocalDate.parse("2024-01-01"),
                    detail = HabitDetail.IntervalHabitDetail(intervalMinutes = intervalMinutes)
                )
            }
            assertTrue(
                exception.message!!.contains("INTERVAL frequency type requires intervalMinutes to be valid"),
                "Expected error message about INTERVAL validation, got: ${exception.message}"
            )
        }
    }
}