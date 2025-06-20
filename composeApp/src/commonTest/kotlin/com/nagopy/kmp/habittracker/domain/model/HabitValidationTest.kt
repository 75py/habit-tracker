package com.nagopy.kmp.habittracker.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
                frequencyType = FrequencyType.INTERVAL,
                intervalMinutes = intervalMinutes
            )
            assertEquals(intervalMinutes, habit.intervalMinutes)
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
                    frequencyType = FrequencyType.INTERVAL,
                    intervalMinutes = intervalMinutes
                )
            }
            assertEquals(
                "INTERVAL frequency type requires intervalMinutes to be a divisor of 60. Valid values: [1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60], got: $intervalMinutes",
                exception.message
            )
        }
    }

    @Test
    fun `Habit creation should succeed with any values for non-INTERVAL types`() {
        // ONCE_DAILY and HOURLY should not be restricted by the divisor validation
        val habit1 = Habit(
            id = 1L,
            name = "Test Habit",
            description = "Test",
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 90 // Invalid for INTERVAL, but OK for ONCE_DAILY
        )
        assertEquals(90, habit1.intervalMinutes)

        val habit2 = Habit(
            id = 2L,
            name = "Test Habit 2",
            description = "Test",
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.HOURLY,
            intervalMinutes = 120 // Invalid for INTERVAL, but OK for HOURLY
        )
        assertEquals(120, habit2.intervalMinutes)
    }
}