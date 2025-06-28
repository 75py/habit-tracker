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
            assertEquals(FrequencyType.INTERVAL, habit.frequencyType)
            assertTrue(habit.detail is HabitDetail.IntervalHabitDetail)
            assertEquals(intervalMinutes, habit.detail.intervalMinutes)
        }
    }

    @Test
    fun `Habit creation should fail with invalid INTERVAL values`() {
        val invalidIntervalMinutes = listOf(7, 8, 9, 11, 13, 14, 16, 17, 18, 19, 21, 25, 45, 90, 120)
        
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
            assertEquals(
                "INTERVAL frequency type requires intervalMinutes to be a divisor of 60. Valid values: [1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60], got: $intervalMinutes",
                exception.message
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
        assertEquals(FrequencyType.ONCE_DAILY, habit.frequencyType)
        assertTrue(habit.detail is HabitDetail.OnceDailyHabitDetail)
        assertEquals(1, habit.detail.scheduledTimes.size) // Should have exactly one scheduled time
        assertEquals(LocalTime(9, 0), habit.detail.scheduledTimes[0])
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
            assertEquals(FrequencyType.ONCE_DAILY, habit.frequencyType)
            assertTrue(habit.detail is HabitDetail.OnceDailyHabitDetail)
            assertEquals(time, habit.detail.scheduledTimes[0])
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
                detail = HabitDetail.HourlyHabitDetail(intervalMinutes = intervalMinutes)
            )
            assertEquals(FrequencyType.HOURLY, habit.frequencyType)
            assertTrue(habit.detail is HabitDetail.HourlyHabitDetail)
            assertEquals(intervalMinutes, habit.detail.intervalMinutes)
        }
    }

    @Test
    fun `Habit creation should fail with invalid HOURLY values`() {
        val invalidIntervalMinutes = listOf(30, 45, 50, 70, 90, 110, 130, 150, 170, 190, 210, 230, 250)
        
        invalidIntervalMinutes.forEach { intervalMinutes ->
            val exception = assertFailsWith<IllegalArgumentException> {
                Habit(
                    id = 1L,
                    name = "Test Habit",
                    description = "Test",
                    createdAt = LocalDate.parse("2024-01-01"),
                    detail = HabitDetail.HourlyHabitDetail(intervalMinutes = intervalMinutes)
                )
            }
            assertEquals(
                "HOURLY frequency type requires intervalMinutes to be a multiple of 60. Got: $intervalMinutes",
                exception.message
            )
        }
    }
}