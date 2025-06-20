package com.nagopy.kmp.habittracker.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HabitIntervalValidatorTest {

    @Test
    fun `isValidIntervalMinutes should return true for valid divisors of 60`() {
        val validValues = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60)
        
        validValues.forEach { value ->
            assertTrue(
                HabitIntervalValidator.isValidIntervalMinutes(value),
                "Expected $value to be valid divisor of 60"
            )
        }
    }

    @Test
    fun `isValidIntervalMinutes should return false for invalid values`() {
        val invalidValues = listOf(7, 8, 9, 11, 13, 14, 16, 17, 18, 19, 21, 25, 45, 90, 120)
        
        invalidValues.forEach { value ->
            assertFalse(
                HabitIntervalValidator.isValidIntervalMinutes(value),
                "Expected $value to be invalid divisor of 60"
            )
        }
    }

    @Test
    fun `getClosestValidIntervalMinutes should return closest valid value`() {
        assertEquals(1, HabitIntervalValidator.getClosestValidIntervalMinutes(0))
        assertEquals(1, HabitIntervalValidator.getClosestValidIntervalMinutes(-5))
        assertEquals(6, HabitIntervalValidator.getClosestValidIntervalMinutes(7))  // Closer to 6 than 5
        assertEquals(6, HabitIntervalValidator.getClosestValidIntervalMinutes(8))  // Closer to 6 than 10
        assertEquals(10, HabitIntervalValidator.getClosestValidIntervalMinutes(11)) // Closer to 10 than 12
        assertEquals(20, HabitIntervalValidator.getClosestValidIntervalMinutes(25)) // Closer to 20 than 30
        assertEquals(30, HabitIntervalValidator.getClosestValidIntervalMinutes(45)) // Closer to 30 than 60
        assertEquals(60, HabitIntervalValidator.getClosestValidIntervalMinutes(90)) // Closer to 60 than 30
    }

    @Test
    fun `VALID_INTERVAL_MINUTES should contain all divisors of 60`() {
        val expectedDivisors = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60)
        assertEquals(expectedDivisors, HabitIntervalValidator.VALID_INTERVAL_MINUTES)
    }

    @Test
    fun `all values in VALID_INTERVAL_MINUTES should actually divide 60 evenly`() {
        HabitIntervalValidator.VALID_INTERVAL_MINUTES.forEach { value ->
            assertEquals(0, 60 % value, "60 should be divisible by $value")
        }
    }
}