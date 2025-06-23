package com.nagopy.kmp.habittracker.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HabitIntervalValidatorTest {

    @Test
    fun `isValidIntervalMinutes should work correctly for INTERVAL frequency type`() {
        // Sub-hour intervals (divisors of 60)
        val validSubHourValues = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60)
        // Multi-hour intervals (multiples of 60)
        val validMultiHourValues = listOf(120, 180, 240, 300, 360)
        val validValues = validSubHourValues + validMultiHourValues
        
        // Invalid values (neither divisors nor multiples of 60)
        val invalidValues = listOf(7, 8, 9, 11, 13, 14, 16, 17, 18, 19, 21, 25, 45, 90) // 90 is not a multiple of 60
        
        validValues.forEach { value ->
            assertTrue(
                HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.INTERVAL, value),
                "Expected $value to be valid for unified INTERVAL type (either divisor or multiple of 60)"
            )
        }
        
        invalidValues.forEach { value ->
            assertFalse(
                HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.INTERVAL, value),
                "Expected $value to be invalid for unified INTERVAL type"
            )
        }
    }

    @Test
    fun `isValidIntervalMinutes should work correctly for ONCE_DAILY frequency type`() {
        assertTrue(
            HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.ONCE_DAILY, 1440),
            "Expected 1440 to be valid for ONCE_DAILY type"
        )
        
        val invalidValues = listOf(720, 1200, 1439, 1441, 2880)
        
        invalidValues.forEach { value ->
            assertFalse(
                HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.ONCE_DAILY, value),
                "Expected $value to be invalid for ONCE_DAILY type"
            )
        }
    }

    @Test
    fun `getClosestValidIntervalMinutes should work correctly for INTERVAL frequency type`() {
        assertEquals(1, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 0))
        assertEquals(1, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, -5))
        assertEquals(6, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 7))  // Closer to 6 than 5
        assertEquals(6, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 8))  // Closer to 6 than 10
        assertEquals(10, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 11)) // Closer to 10 than 12
        assertEquals(20, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 25)) // Closer to 20 than 30
        assertEquals(30, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 45)) // Closer to 30 than 60
        assertEquals(60, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 90)) // Closer to 60 than 30
    }

    @Test
    fun `getClosestValidIntervalMinutes should work correctly for multi-hour INTERVAL type`() {
        // Test sub-hour values (should find closest in VALID_SUB_HOUR_INTERVAL_MINUTES)
        assertEquals(1, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 0))
        assertEquals(1, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, -5))
        assertEquals(30, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 30))  // Exact match
        assertEquals(30, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 45))  // Closer to or equal distance - picks first in list
        assertEquals(20, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 22))  // Closer to 20
        
        // Test multi-hour values (should round to nearest hour, minimum 2 hours)
        assertEquals(120, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 90))  // 1.5 hours rounds to 2 hours
        assertEquals(180, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 150)) // 2.5 hours rounds to 3 hours
        assertEquals(240, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 210)) // 3.5 hours rounds to 4 hours
    }

    @Test
    fun `getClosestValidIntervalMinutes should work correctly for ONCE_DAILY frequency type`() {
        assertEquals(1440, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.ONCE_DAILY, 0))
        assertEquals(1440, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.ONCE_DAILY, 720))
        assertEquals(1440, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.ONCE_DAILY, 1200))
        assertEquals(1440, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.ONCE_DAILY, 1440))
        assertEquals(1440, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.ONCE_DAILY, 2880))
    }


    @Test
    fun `VALID_SUB_HOUR_INTERVAL_MINUTES should contain all divisors of 60`() {
        val expectedDivisors = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60)
        assertEquals(expectedDivisors, HabitIntervalValidator.VALID_SUB_HOUR_INTERVAL_MINUTES)
    }

    @Test
    fun `VALID_ONCE_DAILY_MINUTES should be 1440`() {
        assertEquals(1440, HabitIntervalValidator.VALID_ONCE_DAILY_MINUTES)
    }

    @Test
    fun `all values in VALID_SUB_HOUR_INTERVAL_MINUTES should actually divide 60 evenly`() {
        HabitIntervalValidator.VALID_SUB_HOUR_INTERVAL_MINUTES.forEach { value ->
            assertEquals(0, 60 % value, "60 should be divisible by $value")
        }
    }
}